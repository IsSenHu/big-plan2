package com.gapache.job.common.utils;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author HuSen
 * @since 2021/2/4 10:18 上午
 */
public class ConnectionCache {

    private static final ConcurrentMap<String, ChannelHandlerContext> CONNECTION_CACHE = new ConcurrentHashMap<>();

    public static void save(String clientId, ChannelHandlerContext connection) {
        CONNECTION_CACHE.put(clientId, connection);
    }

    public static ChannelHandlerContext get(String clientId) {
        return CONNECTION_CACHE.get(clientId);
    }
}
