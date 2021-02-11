package com.gapache.job.server.discovery;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.job.common.model.ClientInfo;
import com.gapache.job.server.service.JobGroupService;
import com.gapache.redis.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author HuSen
 * @since 2021/2/4 10:33 上午
 */
@Slf4j
@Component
public class ExecutorDiscovery implements SmartInitializingSingleton {

    private static final String GROUP = "EXECUTOR";

    @Resource
    private JobGroupService jobGroupService;

    @Resource
    private NamingService namingService;

    @Override
    public void afterSingletonsInstantiated() {

    }

    public void scan(ClientInfo clientInfo) {
        DistributedLock lock = DistributedLock.getLock("SCAN_EXECUTOR:" + clientInfo.getAppName());
        try {
            final long timeout = 2;
            if (lock.tryLock(timeout, TimeUnit.SECONDS)) {
                List<Instance> instances = namingService.selectInstances(clientInfo.getAppName(), GROUP, true);
                jobGroupService.checkAndSave(clientInfo, instances);

                // 监听
                namingService.subscribe(clientInfo.getAppName(), GROUP, event -> {
                    if (event instanceof NamingEvent) {
                        NamingEvent namingEvent = (NamingEvent) event;
                        log.info(">>>>>>>>>>>> executor service change:{}", clientInfo.getAppName());
                        jobGroupService.listening(clientInfo.getAppName(), namingEvent.getInstances());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
