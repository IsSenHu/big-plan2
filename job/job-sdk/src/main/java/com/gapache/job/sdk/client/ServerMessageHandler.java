package com.gapache.job.sdk.client;

import com.gapache.job.common.model.ServerMessage;
import com.gapache.job.common.model.TaskInfo;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author HuSen
 * @since 2021/2/4 1:45 下午
 */
@Slf4j
public class ServerMessageHandler extends SimpleChannelInboundHandler<ServerMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ServerMessage serverMessage) {
        int type = serverMessage.getType();
        ServerMessage.Type check = ServerMessage.Type.check(type);
        if (check == null) {
            return;
        }
        switch (check) {
            case TASK: {
                TaskInfo taskInfo = ProtocstuffUtils.byte2Bean(serverMessage.getData(), TaskInfo.class);
                TaskExecutor taskExecutor = new TaskExecutor(taskInfo, serverMessage.getMessageId(), channelHandlerContext::writeAndFlush);

                switch (taskInfo.getBlockingStrategy()) {
                    case COVER: {
                        // 清空队列。
                        TaskPuller.clear(taskInfo.getName());
                        // 覆盖之前的任务阻塞策略
                        TaskPuller.push(taskInfo.getName(), taskExecutor);
                        break;
                    }
                    case DISCARD: {
                        // 丢弃后续的任务
                        if (!TaskPuller.isEmpty(taskInfo.getName())) {
                            break;
                        }
                        TaskPuller.push(taskInfo.getName(), taskExecutor);
                        break;
                    }
                    default: {
                        // 将任务放进队列里面-默认的单机串行
                        TaskPuller.push(taskInfo.getName(), taskExecutor);
                    }
                }
                break;
            }
            case CLOSE:
            default:
        }
    }
}
