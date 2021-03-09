package com.gapache.job.server.server.zeus;

import com.alibaba.fastjson.JSON;
import com.gapache.job.common.model.ClientInfo;
import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.ServerMessage;
import com.gapache.job.sdk.client.zeus.server.JobServerClient;
import com.gapache.job.server.callback.TaskResultCallback;
import com.gapache.job.server.discovery.ExecutorDiscovery;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import com.gapache.vertx.core.VertxManager;
import com.gapache.vertx.web.annotation.RequestRouting;
import com.gapache.vertx.web.annotation.VertxController;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * @author HuSen
 * @since 2021/3/3 6:06 下午
 */
@Slf4j
@VertxController
@RequestRouting("/jobMonitor")
public class JobServerClientImpl implements SmartInitializingSingleton, JobServerClient {

    private static final String CLIENT_MESSAGE_CALLBACK_ADDRESS = "client.message.callback.address";
    private static final String CLIENT_INFO_SCAN_ADDRESS = "client.info.scan.address";

    private final ExecutorDiscovery executorDiscovery;
    private EventConsumerVerticle<String> eventConsumerVerticle;
    private EventConsumerVerticle<String> clientInfoEventConsumerVerticle;

    public JobServerClientImpl(ExecutorDiscovery executorDiscovery) {
        this.executorDiscovery = executorDiscovery;
    }

    @Override
    public Future<ServerMessage> send(ClientMessage clientMessage) {
        return Future.future(promise -> {
            ClientMessage.Type type = ClientMessage.Type.checkType(clientMessage.getType());
            if (type == null) {
                // 非法的类型，直接丢弃该连接
                ServerMessage message = new ServerMessage();
                message.setCode(ServerMessage.ERROR);
                message.setError("不支持的ClientMessage#Type");
                message.setMessageId(clientMessage.getMessageId());
                promise.complete(message);
                return;
            }

            switch (type) {
                // 注册
                case REGISTRY: {
                    ClientInfo clientInfo = ProtocstuffUtils.byte2Bean(clientMessage.getData(), ClientInfo.class);
                    log.info("REGISTRY {}", clientInfo);
                    // 改异步
                    clientInfoEventConsumerVerticle.getEb().publish(CLIENT_INFO_SCAN_ADDRESS, JSON.toJSONString(clientInfo));
                    ServerMessage message = new ServerMessage();
                    message.setCode(ServerMessage.SUCCESS);
                    message.setMessageId(clientMessage.getMessageId());
                    promise.complete(message);
                    break;
                }
                // 任务调度结果
                case RESULT: {
                    log.info("RESULT {}", clientMessage);
                    ServerMessage message = new ServerMessage();
                    message.setCode(ServerMessage.SUCCESS);
                    message.setMessageId(clientMessage.getMessageId());
                    eventConsumerVerticle.getEb().publish(CLIENT_MESSAGE_CALLBACK_ADDRESS, JSON.toJSONString(clientMessage));
                    promise.complete(message);
                    break;
                }
                default: promise.fail("un support message type!!");
            }
        });
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.eventConsumerVerticle = new EventConsumerVerticle<>(CLIENT_MESSAGE_CALLBACK_ADDRESS, message -> {
            String body = message.body();
            ClientMessage clientMessage = JSON.parseObject(body, ClientMessage.class);
            if (clientMessage != null) {
                VertxManager.getVertx().sharedData().<String, String>getAsyncMap(TaskResultCallback.ZEUS_TASK_RESULT_CALLBACK_CACHE)
                        .onSuccess(am -> am.get(clientMessage.getMessageId()).onSuccess(str -> {
                            System.out.println(str);
                            System.out.println(clientMessage.getMessageId());
                            TaskResultCallback callback = new TaskResultCallback(new JsonObject(str).getInteger("retryTime"));
                            callback.callback(clientMessage);
                        }));
            }
        });

        this.clientInfoEventConsumerVerticle = new EventConsumerVerticle<>(CLIENT_INFO_SCAN_ADDRESS, message -> {
            String body = message.body();
            ClientInfo clientInfo = JSON.parseObject(body, ClientInfo.class);
            if (clientInfo != null) {
                executorDiscovery.scan(clientInfo);
            }
        });

        VertxManager
                .getVertx()
                .deployVerticle(eventConsumerVerticle, new DeploymentOptions().setWorker(true));

        VertxManager
                .getVertx()
                .deployVerticle(clientInfoEventConsumerVerticle, new DeploymentOptions().setWorker(true));
    }
}
