package com.gapache.job.sdk;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.job.sdk.client.InnerClient;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HuSen
 * @since 2021/2/5 2:27 下午
 */
@Slf4j
public class JobServerChecker implements InitializingBean {

    @Resource
    private ApplicationContext applicationContext;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosAddress;

    private NamingService namingService;

    @Override
    public void afterPropertiesSet() throws Exception {
        namingService = NacosFactory.createNamingService(nacosAddress);
    }

    @Scheduled(fixedDelay = 10000, initialDelay = 30000)
    public void check() {
        try {
            List<Instance> instances = namingService.selectInstances("job-server", true);
            for (Instance instance : instances) {
                if (JobServerRepository.isNew(instance)) {
                    // 与新服务器建立连接
                    InnerClient innerClient = applicationContext.getBean(InnerClient.class);
                    innerClient.createByInstance(instance);
                    JobServerRepository.saveAll(Lists.newArrayList(instance));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
