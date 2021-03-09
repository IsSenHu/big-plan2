package com.gapache.job.server.trigger;

import com.gapache.commons.utils.ContextUtils;
import com.gapache.job.common.model.RouteStrategy;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;
import com.gapache.job.server.server.ServerMessageSender;
import com.gapache.redis.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author HuSen
 * @since 2021/2/3 5:50 下午
 */
@Slf4j
public class TriggerJob implements Job {

    private static final long TRY_TIMEOUT = 60;

    private static final String VERSION = "EXECUTOR_VERSION:";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        JobGroupEntity group = (JobGroupEntity) jobDataMap.get("group");
        JobEntity job = (JobEntity) jobDataMap.get("job");
        String params = jobDataMap.getString("params");
        int port = jobDataMap.getInt("port");
        String lock = "JOB:" + group.getAppName() + ":" + job.getName();
        RouteStrategy routeStrategy = job.getRouteStrategy();
        ServerMessageSender serverMessageSender = ContextUtils.getApplicationContext().getBean(ServerMessageSender.class);
        if (RouteStrategy.BROADCAST.equals(routeStrategy)) {
            lockToDo(lock, version -> serverMessageSender.broadcastMessage(job, group, version, params, port));
        } else {
            lockToDo(lock, version -> serverMessageSender.sendMessageToDestination(job, group, version, params, port));
        }
    }

    private void lockToDo(String key, Consumer<String> consumer) {
        DistributedLock lock = DistributedLock.getLock(key);
        // 先获取版本号
        String versionKey = VERSION + key;
        ApplicationContext applicationContext = ContextUtils.getApplicationContext();
        StringRedisTemplate redisTemplate = applicationContext.getBean(StringRedisTemplate.class);
        String version = redisTemplate.opsForValue().get(versionKey);
        try {
            if (lock.tryLock(TRY_TIMEOUT, TimeUnit.SECONDS)) {
                // 判断其他节点是否已经执行过这次任务了
                String current = redisTemplate.opsForValue().get(versionKey);
                if (!StringUtils.equals(version, current)) {
                    return;
                }
                consumer.accept(version);
            }
        } catch (Exception e) {
            log.error("分布式锁发生异常.", e);
        } finally {
            // 修改版本号
            redisTemplate.opsForValue().increment(versionKey);
            lock.unlock();
        }
    }
}
