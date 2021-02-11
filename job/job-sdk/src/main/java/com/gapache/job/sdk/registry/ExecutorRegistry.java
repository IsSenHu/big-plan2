package com.gapache.job.sdk.registry;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.job.common.model.JobInfo;
import com.gapache.job.common.thread.JobThreadBuilder;
import com.gapache.job.common.utils.IpUtil;
import com.gapache.job.sdk.JobTrigger;
import com.gapache.job.sdk.ZzhJobRepository;
import com.gapache.job.sdk.annotation.ZzhJob;
import com.gapache.job.sdk.client.TaskWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author HuSen
 * @since 2021/2/4 10:58 上午
 */
@Slf4j
public class ExecutorRegistry implements SmartInitializingSingleton, ApplicationListener<RegistryEvent> {

    private static final String GROUP = "EXECUTOR";

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosAddress;

    private NamingService namingService;

    @Resource
    private ApplicationContext applicationContext;

    private final AtomicBoolean workerCreated = new AtomicBoolean(false);

    @Override
    public void afterSingletonsInstantiated() {
        Assert.hasText(applicationName, "applicationName must need");
        Assert.hasText(nacosAddress, "nacosAddress must need");
        try {
            namingService = NacosFactory.createNamingService(nacosAddress);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onApplicationEvent(@NonNull RegistryEvent event) {
        String clientId = (String) event.getSource();

        log.info("client id {} registry nacos", clientId);
        Instance instance = new Instance();
        instance.setServiceName(applicationName);
        instance.setIp(IpUtil.getIp());
        // 假的端口
        instance.setPort(event.getLocalPort());

        Map<String, JobTrigger> jobTriggerMap = applicationContext.getBeansOfType(JobTrigger.class);
        Map<String, JobInfo> nameJobInfoMap = new HashMap<>(jobTriggerMap.size());

        jobTriggerMap.forEach((beanName, bean) -> {
            log.info("found job trigger {}", beanName);
            ZzhJob zzhJob = AnnotationUtils.findAnnotation(bean.getClass(), ZzhJob.class);
            if (zzhJob != null) {
                Assert.state(!nameJobInfoMap.containsKey(zzhJob.name()), "重复的Job名称:" + zzhJob.name());
                Assert.hasText(zzhJob.name(), "name is required");
                Assert.hasText(zzhJob.author(), "author is required");
                Assert.hasText(zzhJob.cron(), "cron is required");
                Assert.hasText(zzhJob.description(), "description is required");
                Assert.notNull(zzhJob.status(), "status is required");
                Assert.notNull(zzhJob.blockingStrategy(), "blockingStrategy is required");
                Assert.notNull(zzhJob.routeStrategy(), "routeStrategy is required");

                JobInfo jobInfo = new JobInfo();
                jobInfo.setName(zzhJob.name());
                jobInfo.setAuthor(zzhJob.author());
                jobInfo.setCron(zzhJob.cron());
                jobInfo.setDescription(zzhJob.description());
                jobInfo.setStatus(zzhJob.status());
                jobInfo.setBlockingStrategy(zzhJob.blockingStrategy());
                jobInfo.setRetryTimes(zzhJob.retryTimes());
                jobInfo.setRouteStrategy(zzhJob.routeStrategy());
                jobInfo.setEmail(zzhJob.email());
                nameJobInfoMap.put(jobInfo.getName(), jobInfo);
                ZzhJobRepository.save(jobInfo.getName(), bean);
            } else {
                log.warn("job trigger {} not found ZzhJob Annotation", beanName);
            }
        });

        Map<String, String> metaData = new HashMap<>(2);
        if (!nameJobInfoMap.isEmpty()) {
            metaData.put("jobs", JSON.toJSONString(nameJobInfoMap.values()));
        }

        metaData.put("client.id", clientId);
        instance.setMetadata(metaData);
        try {
            namingService.registerInstance(applicationName, GROUP, instance);
        } catch (NacosException e) {
            log.error("registerInstance error.", e);
        }

        if (!workerCreated.get()) {
            if (workerCreated.compareAndSet(false, true)) {
                List<String> names = nameJobInfoMap.values().stream().map(JobInfo::getName).collect(Collectors.toList());
                new JobThreadBuilder()
                        .create(new TaskWorker(names))
                        .daemon(true)
                        .buildAndStart();
            }
        }
    }
}
