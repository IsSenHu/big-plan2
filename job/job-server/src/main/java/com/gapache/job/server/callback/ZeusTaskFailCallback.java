package com.gapache.job.server.callback;

import com.gapache.job.common.Callback;
import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.TaskResult;
import com.gapache.job.common.model.ZeusServerMessage;
import com.gapache.job.sdk.client.zeus.client.ExecutorClient;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import com.gapache.vertx.core.VertxManager;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

/**
 * @author HuSen
 * @since 2021/3/4 3:50 下午
 */
public class ZeusTaskFailCallback implements Callback {

    private final ExecutorClient executorClient;
    private final ZeusServerMessage zeusServerMessage;
    private final int retryTimes;

    public ZeusTaskFailCallback(ExecutorClient executorClient, ZeusServerMessage zeusServerMessage, int retryTimes) {
        this.executorClient = executorClient;
        this.zeusServerMessage = zeusServerMessage;
        this.retryTimes = retryTimes;
    }

    @Override
    public void callback(ClientMessage message) {
        System.out.println("ZeusTaskFailCallback " + message.getMessageId());
        // 把自己保存回去
        VertxManager.getVertx().sharedData().<String, String>getAsyncMap(TaskResultCallback.ZEUS_TASK_FAIL_CALLBACK_CACHE)
                .onSuccess(res -> res.put(message.getMessageId(), new JsonObject().put("retryTimes", retryTimes).put("zeusServerMessage", zeusServerMessage).toString())
                        .onSuccess(putRes -> {
                            // 再次尝试请求执行发送消息
                            TaskResult taskResult = ProtocstuffUtils.byte2Bean(message.getData(), TaskResult.class);
                            taskResult.setExecutorTime(LocalDateTime.now());
                            message.setData(ProtocstuffUtils.bean2Byte(taskResult, TaskResult.class));

                            executorClient.send(zeusServerMessage).onFailure(event -> new TaskResultCallback(retryTimes).callback(message));
                        }));
    }
}
