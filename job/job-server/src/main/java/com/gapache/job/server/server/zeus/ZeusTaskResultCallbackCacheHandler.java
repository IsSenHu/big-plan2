package com.gapache.job.server.server.zeus;

import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.ZeusServerMessage;
import com.gapache.job.sdk.client.zeus.client.ExecutorClient;
import com.gapache.job.server.dao.entity.JobLogEntity;
import com.gapache.vertx.redis.support.SimpleRedisRepository;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author HuSen
 * @since 2021/3/8 6:58 下午
 */
@Slf4j
public class ZeusTaskResultCallbackCacheHandler implements Handler<AsyncResult<Void>> {

    private final ZeusServerMessage zeusServerMessage;
    private final ExecutorClient executorClient;
    private final SimpleRedisRepository simpleRedisRepository;

    public ZeusTaskResultCallbackCacheHandler(ZeusServerMessage zeusServerMessage, ExecutorClient executorClient, SimpleRedisRepository simpleRedisRepository) {
        this.zeusServerMessage = zeusServerMessage;
        this.executorClient = executorClient;
        this.simpleRedisRepository = simpleRedisRepository;
    }

    @Override
    public void handle(AsyncResult<Void> event) {
        if (event.succeeded()) {
            // 发送调度请求
            executorClient.send(zeusServerMessage)
                    .onSuccess(zeusClientMessage -> {
                        ClientMessage clientMessage = zeusClientMessage.getClientMessage();
                        // 调度成功，保存调度结果
                        JobLogEntity newJobLog = new JobLogEntity();
                        newJobLog.setId(zeusServerMessage.getMessage().getMessageId());
                        newJobLog.setTriggerResult(clientMessage.success());
                        simpleRedisRepository.save(newJobLog);
                    })
                    .onFailure(err5 -> {
                        log.error("调度结果失败:{}.", zeusServerMessage.getMessage(), err5);
                        JobLogEntity newJobLog = new JobLogEntity();
                        newJobLog.setId(zeusServerMessage.getMessage().getMessageId());
                        newJobLog.setTriggerResult(false);
                        simpleRedisRepository.save(newJobLog);
                    });
        }
    }
}
