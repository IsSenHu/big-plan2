package com.gapache.job.sdk.client;

import com.gapache.job.common.model.ServerMessage;
import com.gapache.protobuf.utils.ProtocstuffUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author HuSen
 * @since 2021/2/4 1:42 下午
 */
public class ServerMessageDecodeHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        if (bytes.length > 0) {
            byteBuf.getBytes(0, bytes);
            byteBuf.skipBytes(bytes.length);
            list.add(ProtocstuffUtils.byte2Bean(bytes, ServerMessage.class));
        }
    }
}
