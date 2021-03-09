package com.gapache.job.server.callback;

import com.alibaba.fastjson.JSON;
import com.gapache.commons.utils.ContextUtils;
import com.gapache.job.common.Callback;
import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.TaskResult;
import com.gapache.job.common.model.ZeusServerMessage;
import com.gapache.job.sdk.client.zeus.client.ExecutorClient;
import com.gapache.job.server.dao.entity.JobLogEntity;
import com.gapache.job.server.server.EventConsumerManager;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import com.gapache.vertx.core.VertxManager;
import com.gapache.vertx.redis.support.SimpleRedisRepository;
import com.gapache.vertx.redis.support.SuccessType;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * @author HuSen
 * @since 2021/2/4 4:39 下午
 */
@Slf4j
public class TaskResultCallback implements Callback {
    public static final String ZEUS_TASK_FAIL_CALLBACK_CACHE =  "ZeusTaskFailCallbackCache";
    public static final String ZEUS_TASK_RESULT_CALLBACK_CACHE = "ZeusTaskResultCallbackCache";
    private final int retryTimes;

    public TaskResultCallback(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    @Override
    public void callback(ClientMessage message) {
        if (message.getType() == ClientMessage.Type.RESULT.getType()) {
            byte[] data = message.getData();
            ApplicationContext applicationContext = ContextUtils.getApplicationContext();
            ExecutorClient executorClient = applicationContext.getBean(ExecutorClient.class);
            SimpleRedisRepository simpleRedisRepository = applicationContext.getBean(SimpleRedisRepository.class);
            simpleRedisRepository.findById(message.getMessageId(), JobLogEntity.class)
                    .onSuccess(jobLog -> {
                        if (jobLog == null) {
                            log.error("没有找到对应的日志:{}.", message.getMessageId());
                            return;
                        }
                        TaskResult taskResult = ProtocstuffUtils.byte2Bean(data, TaskResult.class);
                        System.out.println(message.getMessageId() + taskResult + jobLog + retryTimes);
                        // 失败重试，记录重试的次数，并且重新发送该消息
                        if (taskResult.getCode() != TaskResult.SUCCESS && jobLog.getRetryTimes() < retryTimes) {
                            // 重试次数加1
                            jobLog.setRetryTimes(jobLog.getRetryTimes() + 1);
                            simpleRedisRepository.save(jobLog)
                                    .onSuccess(res -> {
                                        if (SuccessType.SET_OK.success(res)) {
                                            // 开始执行失败回调也就是重试
                                            VertxManager.getVertx().sharedData().<String, String>getAsyncMap(ZEUS_TASK_FAIL_CALLBACK_CACHE)
                                                    .onSuccess(map -> {
                                                        // 移除并获取
                                                        map.remove(message.getMessageId())
                                                                .onSuccess(body -> {
                                                                    JsonObject jsonObject = new JsonObject(body);
                                                                    Integer retryTime = jsonObject.getInteger("retryTime");
                                                                    ZeusServerMessage zeusServerMessage = JSON.parseObject(jsonObject.getJsonObject("zeusServerMessage").toString(), ZeusServerMessage.class);
                                                                    new ZeusTaskFailCallback(executorClient, zeusServerMessage, retryTime).callback(message);
                                                                });
                                                    });
                                        }
                                    });
                        } else {
                            jobLog.setExecutorResult(taskResult.getCode() == TaskResult.SUCCESS);
                            jobLog.setExecutorTime(taskResult.getExecutorTime());
                            jobLog.setExecutorRemark(taskResult.getRemark());
                            // 移除不需要的failCallBack
                            VertxManager.getVertx().sharedData().<String, String>getAsyncMap(ZEUS_TASK_FAIL_CALLBACK_CACHE)
                                    .onSuccess(map -> map.remove(message.getMessageId()));
                            // 移除不需要的resultCallback
                            System.out.println("REMOVE: " + message.getMessageId());
                            // 广播模式下移除会有问题
                            VertxManager.getVertx().sharedData().<String, String>getAsyncMap(ZEUS_TASK_RESULT_CALLBACK_CACHE)
                                    .onSuccess(map -> map.remove(message.getMessageId()));
                            // 发布最终的处理事件
                            VertxManager.getVertx().eventBus().send(EventConsumerManager.JOB_FINALLY_RESULT_DEAL_ADDRESS, JSON.toJSONString(jobLog));
                        }
                    })
                    .onFailure(error -> log.error("没有找到对应的日志:{}.", message.getMessageId(), error));
        }
    }
}
