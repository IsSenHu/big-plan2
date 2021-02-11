package com.gapache.job.server.server;

import com.gapache.job.common.model.ClientMessage;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author HuSen
 * @since 2021/2/4 10:00 上午
 */
public class ClientMessageDecodeHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        if (bytes.length > 0) {
            byteBuf.getBytes(0, bytes);
            byteBuf.skipBytes(bytes.length);
            list.add(ProtocstuffUtils.byte2Bean(bytes, ClientMessage.class));
        }
    }
}
