package com.gapache.job.sdk.client;

import com.gapache.job.common.model.ClientMessage;
import com.gapache.job.common.model.Constants;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author HuSen
 * @since 2021/2/4 1:40 下午
 */
public class ClientMessageEncodeHandler extends MessageToByteEncoder<ClientMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ClientMessage message, ByteBuf byteBuf) {
        byte[] bytes = ProtocstuffUtils.bean2Byte(message, ClientMessage.class);
        if (bytes != null) {
            byteBuf.writeBytes(bytes);
            byte[] d = Constants.DELIMITER.getBytes();
            byteBuf.writeBytes(d);
        }
    }
}
