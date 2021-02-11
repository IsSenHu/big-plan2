package com.gapache.job.server.trigger;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.commons.utils.ContextUtils;
import com.gapache.job.common.model.RouteStrategy;
import com.gapache.job.common.model.ServerMessage;
import com.gapache.job.common.model.TaskInfo;
import com.gapache.job.common.utils.CallbackCache;
import com.gapache.job.common.utils.ConnectionCache;
import com.gapache.job.common.utils.IpUtil;
import com.gapache.job.server.callback.TaskFailCallback;
import com.gapache.job.server.callback.TaskResultCallback;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;
import com.gapache.job.server.dao.entity.JobLogEntity;
import com.gapache.job.server.dao.repository.JobLogRepository;
import com.gapache.job.server.warner.WarnerUtils;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import com.gapache.redis.lock.DistributedLock;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author HuSen
 * @since 2021/2/3 5:50 下午
 */
@Slf4j
public class TriggerJob implements Job {

    private static final long TRY_TIMEOUT = 60;

    private static final String GROUP = "EXECUTOR";

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
        if (RouteStrategy.BROADCAST.equals(routeStrategy)) {
            lockToDo(lock, version -> broadcastMessage(job, group, version, params, port));
        } else {
            lockToDo(lock, version -> sendMessageToDestination(job, group, version, params, port));
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

    private void sendMessageToDestination(JobEntity job, JobGroupEntity group, String version, String params, int port) {
        JobLogEntity jobLog = new JobLogEntity();
        jobLog.setJobId(job.getId());
        jobLog.setTriggerTime(LocalDateTime.now());
        jobLog.setTriggerResult(false);
        jobLog.setParams(params);
        String messageId = UUID.randomUUID().toString();
        ApplicationContext applicationContext = ContextUtils.getApplicationContext();
        JobLogRepository jobLogRepository = applicationContext.getBean(JobLogRepository.class);
        // 获取到锁的JobServer选择Executor来执行
        NamingService namingService = applicationContext.getBean(NamingService.class);
        try {
            Instance instance;
            RouteStrategy routeStrategy = job.getRouteStrategy();
            switch (routeStrategy) {
                case NACOS: {
                    instance = namingService.selectOneHealthyInstance(group.getAppName(), GROUP);
                    break;
                }
                case FIRST:
                case LAST:
                case RANDOM:
                case ROUND_ROBIN: {
                    List<Instance> instances = namingService.selectInstances(group.getAppName(), GROUP, true);
                    instance = routeStrategy.getRoute().select(instances);
                    break;
                }
                default:
                    throw new IllegalArgumentException(routeStrategy + " 暂不支持的路由类型");
            }
            if (instance != null) {
                sendTaskInfo(group, job, params, port, jobLog, messageId, version, instance);
            } else {
                jobLog.setTriggerRemark("没有可用节点");
                jobLog.setTriggerResult(false);
            }
        } catch (Exception e) {
            jobLog.setTriggerResult(false);
            jobLog.setTriggerRemark(e.toString());
            jobLog.setMessageId(messageId);
            log.error("execute error {} {}", group.getAppName(), job.getName(), e);
            WarnerUtils.warning(group, job, params, port, e.toString());
        }
        try {
            jobLogRepository.save(jobLog);
        } catch (Exception ignored) {
        }
    }

    private void broadcastMessage(JobEntity job, JobGroupEntity group, String version, String params, int port) {
        try {
            ApplicationContext applicationContext = ContextUtils.getApplicationContext();
            JobLogRepository jobLogRepository = applicationContext.getBean(JobLogRepository.class);
            NamingService namingService = applicationContext.getBean(NamingService.class);
            List<Instance> instances = namingService.selectInstances(group.getAppName(), GROUP, true);
            if (CollectionUtils.isEmpty(instances)) {
                throw new RuntimeException("没有找到广播的目的地节点");
            }
            for (Instance instance : instances) {
                JobLogEntity jobLog = new JobLogEntity();
                jobLog.setJobId(job.getId());
                jobLog.setTriggerTime(LocalDateTime.now());
                jobLog.setTriggerResult(false);
                jobLog.setParams(params);
                String messageId = UUID.randomUUID().toString();
                // 给每个节点发送任务调度消息
                sendTaskInfo(group, job, params, port, jobLog, messageId, version, instance);
                jobLogRepository.save(jobLog);
            }
        } catch (Exception e) {
            // LOG ERROR
            log.error("广播时发生异常:", e);
            JobLogEntity jobLog = new JobLogEntity();
            jobLog.setJobId(job.getId());
            jobLog.setTriggerTime(LocalDateTime.now());
            jobLog.setTriggerResult(false);
            jobLog.setParams(params);
            jobLog.setTriggerRemark("广播时发生异常:\r\n" + e.toString());
            ContextUtils.getApplicationContext().getBean(JobLogRepository.class).save(jobLog);
            WarnerUtils.warning(group, job, params, port, e.toString());
        }
    }

    private void sendTaskInfo(JobGroupEntity group, JobEntity job, String params, int port, JobLogEntity jobLog, String messageId, String version, Instance instance) {
        String instanceId = instance.getMetadata().get("client.id");
        ChannelHandlerContext connection = ConnectionCache.get(instanceId);

        ServerMessage message = new ServerMessage();
        message.setMessageId(messageId);

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setName(job.getName());
        taskInfo.setParams(StringUtils.isNotBlank(params) ? params : job.getParams());
        taskInfo.setBlockingStrategy(job.getBlockingStrategy());
        message.setData(ProtocstuffUtils.bean2Byte(taskInfo, TaskInfo.class));
        message.setType(ServerMessage.Type.TASK.getType());

        jobLog.setMessageId(messageId);

        StringBuilder builder = new StringBuilder();
        builder.append("调度节点: ").append(group.getAppName()).append(System.lineSeparator());
        builder.append("节点地址: ").append(instance.getIp()).append(":").append(instance.getPort()).append(System.lineSeparator());
        builder.append("调度次数: ").append(version == null ? 1 : Integer.parseInt(version) + 1).append(System.lineSeparator());
        builder.append("调度中心: ").append(IpUtil.getIp()).append(":").append(port);
        connection.writeAndFlush(message)
                .addListener(future -> {
                    boolean success = futureListen(job, jobLog, connection, message, builder, future);
                    if (!success) {
                        WarnerUtils.warning(group, job, params, port, "调度消息发送失败");
                    }
                });
    }

    private boolean futureListen(JobEntity job, JobLogEntity jobLog, ChannelHandlerContext connection, ServerMessage message, StringBuilder builder, io.netty.util.concurrent.Future<? super Void> future) {
        if (future.isSuccess()) {
            jobLog.setTriggerResult(true);
            jobLog.setTriggerRemark(builder.toString());
            CallbackCache.save(message.getMessageId(), new TaskResultCallback(job.getRetryTimes() != null ? job.getRetryTimes() : 0));
            if (job.getRetryTimes() != null && job.getRetryTimes() > 0) {
                CallbackCache.save("fail:" + message.getMessageId(), new TaskFailCallback(message, connection));
            }
            return true;
        } else {
            jobLog.setTriggerRemark(builder.toString());
            jobLog.setTriggerResult(false);
            return false;
        }
    }
}
