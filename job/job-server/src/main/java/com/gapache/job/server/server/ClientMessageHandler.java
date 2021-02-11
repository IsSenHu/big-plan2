package com.gapache.job.server.server;

import com.gapache.job.common.Callback;
import com.gapache.job.common.model.ClientInfo;
import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.ServerMessage;
import com.gapache.job.common.utils.CallbackCache;
import com.gapache.job.common.utils.ConnectionCache;
import com.gapache.job.server.discovery.ExecutorDiscovery;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 主要的处理逻辑在这个Handler里面
 *
 * @author HuSen
 * @since 2021/2/4 10:05 上午
 */
@Slf4j
public class ClientMessageHandler extends SimpleChannelInboundHandler<ClientMessage> {

    private final ThreadPoolExecutor threadPoolExecutor;
    private final ExecutorDiscovery executorDiscovery;

    public ClientMessageHandler(ThreadPoolExecutor threadPoolExecutor, ExecutorDiscovery executorDiscovery) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.executorDiscovery = executorDiscovery;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClientMessage clientMessage) {
        ClientMessage.Type type = ClientMessage.Type.checkType(clientMessage.getType());
        if (type == null) {
            // 非法的类型，直接丢弃该连接
            ServerMessage message = new ServerMessage();
            message.setCode(ServerMessage.ERROR);
            message.setError("不支持的ClientMessage#Type");
            message.setMessageId(clientMessage.getMessageId());
            channelHandlerContext.writeAndFlush(message);
            return;
        }

        switch (type) {
            // 注册
            case REGISTRY: {
                ClientInfo clientInfo = ProtocstuffUtils.byte2Bean(clientMessage.getData(), ClientInfo.class);
                log.info("REGISTRY {}", clientInfo);
                ConnectionCache.save(clientInfo.getClientId(), channelHandlerContext);
                ServerMessage message = new ServerMessage();
                message.setCode(ServerMessage.SUCCESS);
                message.setMessageId(clientMessage.getMessageId());

                executorDiscovery.scan(clientInfo);
                channelHandlerContext.writeAndFlush(message);
                break;
            }
            // 任务调度结果
            case RESULT: {
                threadPoolExecutor.execute(() -> {
                    Callback callback = CallbackCache.get(clientMessage.getMessageId());
                    if (callback != null) {
                        callback.callback(clientMessage);
                    }
                });
                break;
            }
            default:
        }
    }
}
