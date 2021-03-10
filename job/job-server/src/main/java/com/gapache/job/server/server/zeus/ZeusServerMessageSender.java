package com.gapache.job.server.server.zeus;

import com.gapache.job.common.model.ServerMessage;
import com.gapache.job.common.model.TaskInfo;
import com.gapache.job.common.model.ZeusServerMessage;
import com.gapache.job.common.utils.IpUtil;
import com.gapache.job.sdk.client.zeus.client.ExecutorClient;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.job.server.dao.entity.JobGroupEntity;
import com.gapache.job.server.dao.entity.JobLogEntity;
import com.gapache.job.server.server.ServerMessageSender;
import com.gapache.job.server.warner.WarnerUtils;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import com.gapache.vertx.redis.support.SimpleRedisRepository;
import com.gapache.vertx.redis.support.SuccessType;
import com.gapache.vertx.web.zeus.CachingSpringLoadBalancerFactory;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author HuSen
 * @since 2021/3/4 3:34 下午
 */
@Slf4j
public class ZeusServerMessageSender implements ServerMessageSender {

    private final CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory;
    private final SimpleRedisRepository simpleRedisRepository;
    private final ExecutorClient executorClient;

    public ZeusServerMessageSender(ApplicationContext applicationContext) {
        cachingSpringLoadBalancerFactory = new CachingSpringLoadBalancerFactory(applicationContext.getBean(SpringClientFactory.class));
        simpleRedisRepository = applicationContext.getBean(SimpleRedisRepository.class);
        executorClient = applicationContext.getBean(ExecutorClient.class);
    }

    @Override
    public void sendMessageToDestination(JobEntity job, JobGroupEntity group, String version, String params, int port) {
        JobLogEntity jobLog = JobLogEntity.of(job.getId());
        jobLog.setParams(params);
        String messageId = UUID.randomUUID().toString();
        jobLog.setId(messageId);

        try {
            ILoadBalancer iLoadBalancer = cachingSpringLoadBalancerFactory.create(group.getAppName());
            Server server = iLoadBalancer.chooseServer(group.getAppName());
            if (server == null) {
                throw new RuntimeException("没有可用的Executor服务");
            }
            ServerIntrospector serverIntrospector = cachingSpringLoadBalancerFactory.createServerIntrospector(group.getAppName());
            Map<String, String> metadata = serverIntrospector.getMetadata(server);

            ZeusServerMessage zeusServerMessage = new ZeusServerMessage();
            zeusServerMessage.setIp(server.getHost());
            zeusServerMessage.setPort(Integer.parseInt(metadata.get("zeus-port")));

            sendTaskInfo(zeusServerMessage, group, job, params, port, jobLog, messageId, version);
        } catch (Exception e) {
            jobLog.setTriggerResult(false);
            jobLog.setTriggerRemark(e.toString());
            log.error("sendMessageToDestination error {} {}", group.getAppName(), job.getName(), e);
            try {
                simpleRedisRepository.save(jobLog)
                        .onSuccess(res -> {
                            if (SuccessType.SET_OK.success(res)) {
                                log.info("保存日志成功:{}", jobLog);
                            }
                        });
            } catch (Exception ignored) {
            }
            WarnerUtils.warning(group, job, params, port, e.toString());
        }
    }

    @Override
    public void broadcastMessage(JobEntity job, JobGroupEntity group, String version, String params, int port) {
        JobLogEntity jobLog = JobLogEntity.of(job.getId());
        jobLog.setParams(params);
        jobLog.setId(UUID.randomUUID().toString());

        try {
            ILoadBalancer iLoadBalancer = cachingSpringLoadBalancerFactory.create(group.getAppName());
            List<Server> reachableServers = iLoadBalancer.getReachableServers();
            ServerIntrospector serverIntrospector = cachingSpringLoadBalancerFactory.createServerIntrospector(group.getAppName());
            if (reachableServers.isEmpty()) {
                throw new IllegalStateException("no reachableServers");
            }
            for (Server reachableServer : reachableServers) {
                String messageId = UUID.randomUUID().toString();
                JobLogEntity log = JobLogEntity.of(job.getId());
                log.setParams(params);
                log.setId(messageId);

                String host = reachableServer.getHost();
                String zeusPort = serverIntrospector.getMetadata(reachableServer).get("zeus-port");

                ZeusServerMessage zeusServerMessage = new ZeusServerMessage();
                zeusServerMessage.setIp(host);
                zeusServerMessage.setPort(Integer.parseInt(zeusPort));
                sendTaskInfo(zeusServerMessage, group, job, params, port, log, messageId, version);
            }
        } catch (Exception e) {
            jobLog.setTriggerResult(false);
            jobLog.setTriggerRemark("广播时发生异常:\r\n" + e.toString());
            log.error("broadcastMessage error {} {}", group.getAppName(), job.getName(), e);
            try {
                simpleRedisRepository.save(jobLog);
            } catch (Exception ignored) {
            }
            WarnerUtils.warning(group, job, params, port, e.toString());
        }
    }

    private void sendTaskInfo(ZeusServerMessage zeusServerMessage, JobGroupEntity group, JobEntity job, String params, int port, JobLogEntity jobLog, String messageId, String version) {
        ServerMessage message = new ServerMessage();
        zeusServerMessage.setMessage(message);

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setName(job.getName());
        taskInfo.setParams(StringUtils.isNotBlank(params) ? params : job.getParams());
        taskInfo.setBlockingStrategy(job.getBlockingStrategy());
        message.setMessageId(messageId);
        message.setData(ProtocstuffUtils.bean2Byte(taskInfo, TaskInfo.class));
        message.setType(ServerMessage.Type.TASK.getType());

        // 调度日志组装
        String builder = "调度节点: " + group.getAppName() + System.lineSeparator() +
                "调度次数: " + (version == null ? 1 : Integer.parseInt(version) + 1) + System.lineSeparator() +
                "调度中心: " + IpUtil.getIp() + ":" + port + System.lineSeparator() +
                "节点地址: " + zeusServerMessage.getIp() + ":" + zeusServerMessage.getPort();
        jobLog.setTriggerRemark(builder);

        // 保存调度任务前的日志
        int retryTime = job.getRetryTimes() != null ? job.getRetryTimes() : 0;
        ZeusTaskResultCallbackCacheHandler zeusTaskResultCallbackCacheHandler = new ZeusTaskResultCallbackCacheHandler(zeusServerMessage, executorClient, simpleRedisRepository);
        ZeusTaskFailCallbackCacheHandler zeusTaskFailCallbackCacheHandler = new ZeusTaskFailCallbackCacheHandler(messageId, retryTime, zeusTaskResultCallbackCacheHandler);
        SaveJobLogHandler saveJobLogHandler = new SaveJobLogHandler(job, messageId, zeusServerMessage, zeusTaskFailCallbackCacheHandler, zeusTaskResultCallbackCacheHandler);

        simpleRedisRepository.save(jobLog)
                .onComplete(saveJobLogHandler);
    }
}
