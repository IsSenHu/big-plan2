package com.gapache.job.server.server;

import com.gapache.job.common.model.Constants;
import com.gapache.job.common.model.ServerMessage;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author HuSen
 * @since 2021/2/4 10:04 上午
 */
public class ServerMessageEncodeHandler extends MessageToByteEncoder<ServerMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ServerMessage serverMessage, ByteBuf byteBuf) {
        byte[] bytes = ProtocstuffUtils.bean2Byte(serverMessage, ServerMessage.class);
        if (bytes != null) {
            byteBuf.writeBytes(bytes);
            byte[] d = Constants.DELIMITER.getBytes();
            byteBuf.writeBytes(d);
        }
    }
}
