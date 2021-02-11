package com.gapache.job.sdk.client;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.job.common.model.ClientInfo;
import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.Constants;
import com.gapache.job.sdk.JobServerRepository;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * @author HuSen
 * create on 2020/9/11 21:53
 */
@Slf4j
public class ClientLifeCycleHandler extends ChannelInboundHandlerAdapter {

    private final String appName;
    private final InnerClient embedClient;
    private final Instance instance;

    public ClientLifeCycleHandler(String appName, InnerClient embedClient, Instance instance) {
        this.appName = appName;
        this.embedClient = embedClient;
        this.instance = instance;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        // 1
        if (log.isDebugEnabled()) {
            log.debug(">>>>>>>>>>>> ClientLifeCycleHandler channelRegistered:{}", this.appName);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        // 4
        log.warn(">>>>>>>>>>>> ClientLifeCycleHandler channelUnregistered:{}", this.appName);
        JobServerRepository.remove(instance.getIp(), instance.getPort());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 2
        if (log.isDebugEnabled()) {
            log.debug(">>>>>>>>>>>> ClientLifeCycleHandler channelActive:{}", this.appName);
        }
        // 注册
        registry(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 3
        log.warn(">>>>>>>>>>>> ClientLifeCycleHandler channelInactive:{}", this.appName);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(">>>>>>>>>>>> ClientLifeCycleHandler exceptionCaught:{}", this.appName, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        log.info("{}", evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            log.info(">>>>>>>>>>>> ClientLifeCycleHandler IdleStateEvent:{}", idleStateEvent);
            registry(ctx);
        }
    }

    private void registry(ChannelHandlerContext ctx) {
        log.info(">>>>>>>>>>>> ClientLifeCycleHandler register:{}", this.appName);
        ClientMessage clientMessage = new ClientMessage();
        clientMessage.setMessageId(UUID.randomUUID().toString());
        clientMessage.setType(ClientMessage.Type.REGISTRY.getType());

        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setAppName(appName);
        clientInfo.setName(embedClient.getName());
        clientInfo.setClientId(embedClient.getClientId());

        clientMessage.setData(ProtocstuffUtils.bean2Byte(clientInfo, ClientInfo.class));

        byte[] bytes = ProtocstuffUtils.bean2Byte(clientMessage, ClientMessage.class);
        Assert.notNull(bytes, "bytes is always not null");
        byte[] d = Constants.DELIMITER.getBytes();
        ByteBuf buffer = Unpooled.buffer(bytes.length + d.length);
        buffer.writeBytes(bytes).writeBytes(d);
        ctx.writeAndFlush(buffer).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                // 注册消息发送失败了
                log.info(">>>>>>>>>>>> ClientLifeCycleHandler 注册消息发送失败了");
                future.channel().close();
            }
        });

        embedClient.registry();
    }
}
