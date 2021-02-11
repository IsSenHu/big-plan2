package com.gapache.job.common;

import com.gapache.commons.security.RSAUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * 数据加解密Handler
 *
 * @author HuSen
 * @since 2021/2/7 5:10 下午
 */
public class EncryptAndDecryptMessageHandler extends MessageToMessageCodec<byte[], byte[]> {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public EncryptAndDecryptMessageHandler(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public EncryptAndDecryptMessageHandler(Class<? extends byte[]> inboundMessageType, Class<? extends byte[]> outboundMessageType, PrivateKey privateKey, PublicKey publicKey) {
        super(inboundMessageType, outboundMessageType);
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, byte[] bytes, List<Object> list) {
        try {
            byte[] encrypt = RSAUtils.encrypt(bytes, privateKey);
            list.add(encrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, byte[] bytes, List<Object> list) {
        try {
            byte[] decrypt = RSAUtils.decrypt(bytes, publicKey);
            list.add(decrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
