package com.gapache.job.server.callback;

import com.gapache.job.common.Callback;
import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.ServerMessage;
import com.gapache.job.common.utils.CallbackCache;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author HuSen
 * @since 2021/2/7 4:00 下午
 */
public class TaskFailCallback implements Callback {

    private final ServerMessage serverMessage;
    private final ChannelHandlerContext channelHandlerContext;

    public TaskFailCallback(ServerMessage serverMessage, ChannelHandlerContext channelHandlerContext) {
        this.serverMessage = serverMessage;
        this.channelHandlerContext = channelHandlerContext;
    }

    @Override
    public void callback(ClientMessage message) {
        // 重发消息进行重试
        channelHandlerContext.writeAndFlush(serverMessage);
        // 把自己保存回去
        CallbackCache.save("fail:" + message.getMessageId(), this);
    }
}
