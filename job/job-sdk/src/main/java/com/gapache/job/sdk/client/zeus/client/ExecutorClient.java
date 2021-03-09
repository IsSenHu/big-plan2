package com.gapache.job.sdk.client.zeus.client;

import com.gapache.job.common.model.ZeusClientMessage;
import com.gapache.job.common.model.ZeusServerMessage;
import com.gapache.vertx.web.annotation.PostRouting;
import com.gapache.vertx.web.annotation.ZeusClient;
import io.vertx.core.Future;

/**
 * @author HuSen
 * @since 2021/3/3 7:42 下午
 */
@ZeusClient(value = "executor", path = "/executor")
public interface ExecutorClient {

    /**
     * 发送ServerMessage到Executor
     *
     * @param serverMessage ZeusServerMessage
     * @return Future<ClientMessage>
     */
    @PostRouting("/send")
    Future<ZeusClientMessage> send(ZeusServerMessage serverMessage);
}
