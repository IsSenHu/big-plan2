package com.gapache.job.server.server.zeus;

import com.gapache.job.common.model.ZeusServerMessage;
import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.vertx.core.VertxManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Response;
import lombok.extern.slf4j.Slf4j;

import static com.gapache.job.server.callback.TaskResultCallback.ZEUS_TASK_FAIL_CALLBACK_CACHE;
import static com.gapache.job.server.callback.TaskResultCallback.ZEUS_TASK_RESULT_CALLBACK_CACHE;

/**
 * @author HuSen
 * @since 2021/3/8 6:52 下午
 */
@Slf4j
public class SaveJobLogHandler implements Handler<AsyncResult<Response>> {
    private final JobEntity job;
    private final String messageId;
    private final ZeusServerMessage zeusServerMessage;
    private final ZeusTaskFailCallbackCacheHandler zeusTaskFailCallbackCacheHandler;
    private final ZeusTaskResultCallbackCacheHandler zeusTaskResultCallbackCacheHandler;

    public SaveJobLogHandler(JobEntity job, String messageId, ZeusServerMessage zeusServerMessage, ZeusTaskFailCallbackCacheHandler zeusTaskFailCallbackCacheHandler, ZeusTaskResultCallbackCacheHandler zeusTaskResultCallbackCacheHandler) {
        this.job = job;
        this.messageId = messageId;
        this.zeusServerMessage = zeusServerMessage;
        this.zeusTaskFailCallbackCacheHandler = zeusTaskFailCallbackCacheHandler;
        this.zeusTaskResultCallbackCacheHandler = zeusTaskResultCallbackCacheHandler;
    }

    @Override
    public void handle(AsyncResult<Response> event) {
        if (event.succeeded()) {
            // 如果有重试，保存失败回调处理程序
            int retryTime = job.getRetryTimes() != null ? job.getRetryTimes() : 0;
            if (retryTime > 0) {
                VertxManager.getVertx().sharedData().<String, String>getAsyncMap(ZEUS_TASK_FAIL_CALLBACK_CACHE)
                        .onSuccess(as1 -> as1.put(messageId, new JsonObject().put("retryTime", retryTime).put("zeusServerMessage", zeusServerMessage).toString())
                                .onComplete(zeusTaskFailCallbackCacheHandler));
            } else {
                // 不重试则手动触发保存
                VertxManager.getVertx().sharedData().<String, String>getAsyncMap(ZEUS_TASK_RESULT_CALLBACK_CACHE)
                        .onSuccess(as3 -> as3.put(messageId, new JsonObject().put("retryTime", retryTime).toString())
                                .onComplete(zeusTaskResultCallbackCacheHandler));
            }
        } else {
            log.error("保存日志失败 {} {}", messageId, job, event.cause());
        }
    }
}
