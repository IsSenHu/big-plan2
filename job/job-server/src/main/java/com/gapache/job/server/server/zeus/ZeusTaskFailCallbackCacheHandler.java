package com.gapache.job.server.server.zeus;

import com.gapache.vertx.core.VertxManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import static com.gapache.job.server.callback.TaskResultCallback.ZEUS_TASK_RESULT_CALLBACK_CACHE;

/**
 * @author HuSen
 * @since 2021/3/8 6:56 下午
 */
@Slf4j
public class ZeusTaskFailCallbackCacheHandler implements Handler<AsyncResult<Void>> {

    public ZeusTaskFailCallbackCacheHandler(String messageId, int retryTime, ZeusTaskResultCallbackCacheHandler zeusTaskResultCallbackCacheHandler) {
        this.messageId = messageId;
        this.retryTime = retryTime;
        this.zeusTaskResultCallbackCacheHandler = zeusTaskResultCallbackCacheHandler;
    }

    private final String messageId;
    private final int retryTime;
    private final ZeusTaskResultCallbackCacheHandler zeusTaskResultCallbackCacheHandler;

    @Override
    public void handle(AsyncResult<Void> event) {
        if (event.succeeded()) {
            VertxManager.getVertx().sharedData().<String, String>getAsyncMap(ZEUS_TASK_RESULT_CALLBACK_CACHE)
                    .onSuccess(as3 -> as3.put(messageId, new JsonObject().put("retryTime", retryTime).toString())
                            .onComplete(zeusTaskResultCallbackCacheHandler));
        } else {
            log.error("保存任务结果回调失败 {}.", messageId, event.cause());
        }
    }
}
