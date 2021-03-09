package com.gapache.job.sdk.client.zeus.server;

import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.ServerMessage;
import com.gapache.vertx.web.annotation.PostRouting;
import com.gapache.vertx.web.annotation.ZeusClient;
import io.vertx.core.Future;

/**
 * @author HuSen
 * @since 2021/3/3 5:32 下午
 */
@ZeusClient(value = "job-server", path = "/jobMonitor")
public interface JobServerClient {

    /**
     * 发送clientMessage到JobServer
     *
     * @param clientMessage 来自JobClient的消息
     * @return Future<ServerMessage>
     */
    @PostRouting("/send")
    Future<ServerMessage> send(ClientMessage clientMessage);
}
