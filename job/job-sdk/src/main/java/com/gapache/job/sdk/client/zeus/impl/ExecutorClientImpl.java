package com.gapache.job.sdk.client.zeus.impl;

import com.gapache.job.common.model.*;
import com.gapache.job.sdk.client.TaskExecutor;
import com.gapache.job.sdk.client.TaskPuller;
import com.gapache.job.sdk.client.zeus.client.ExecutorClient;
import com.gapache.job.sdk.client.zeus.server.JobServerClient;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import com.gapache.vertx.web.annotation.RequestRouting;
import com.gapache.vertx.web.annotation.VertxController;
import io.vertx.core.Future;

import javax.annotation.Resource;

/**
 * @author HuSen
 * @since 2021/3/4 2:21 下午
 */
@VertxController
@RequestRouting("executor")
public class ExecutorClientImpl implements ExecutorClient {

    @Resource
    private JobServerClient jobServerClient;

    @Override
    public Future<ZeusClientMessage> send(ZeusServerMessage serverMessage) {
        return Future.future(event -> {
            ClientMessage clientMessage = new ClientMessage();
            ZeusClientMessage zeusClientMessage = new ZeusClientMessage();
            zeusClientMessage.setClientMessage(clientMessage);
            ServerMessage message = serverMessage.getMessage();
            int type = message.getType();
            ServerMessage.Type check = ServerMessage.Type.check(type);
            if (check == null) {
                clientMessage.setCode(5000);
                clientMessage.setError("not support type: " + type);
                event.complete(zeusClientMessage);
                return;
            }

            switch (check) {
                case TASK: {
                    TaskInfo taskInfo = ProtocstuffUtils.byte2Bean(message.getData(), TaskInfo.class);
                    TaskExecutor taskExecutor = new TaskExecutor(taskInfo, message.getMessageId(), cm -> jobServerClient.send(cm).onComplete(TaskExecutor.LOGGER));

                    switch (taskInfo.getBlockingStrategy()) {
                        case DISCARD: {
                            // 丢弃后续的任务
                            if (!TaskPuller.isEmpty(taskInfo.getName())) {
                                break;
                            }
                            TaskPuller.push(taskInfo.getName(), taskExecutor);
                            break;
                        }
                        case COVER: {
                            // 清空队列。
                            TaskPuller.clear(taskInfo.getName());
                            // 覆盖之前的任务阻塞策略
                            TaskPuller.push(taskInfo.getName(), taskExecutor);
                            break;
                        }
                        default: {
                            // 将任务放进队列里面-默认的单机串行
                            TaskPuller.push(taskInfo.getName(), taskExecutor);
                        }
                    }
                    clientMessage.setCode(ClientMessage.SUCCESS);
                    event.complete(zeusClientMessage);
                    break;
                }
                case CLOSE:
                default:
                    clientMessage.setCode(5000);
                    clientMessage.setError("not support type: " + type);
                    event.complete(zeusClientMessage);
            }
        });
    }
}
