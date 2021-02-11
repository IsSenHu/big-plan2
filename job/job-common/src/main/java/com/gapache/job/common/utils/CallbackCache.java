package com.gapache.job.common.utils;

import com.gapache.job.common.Callback;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author HuSen
 * @since 2021/2/4 10:29 上午
 */
public class CallbackCache {

    private static final ConcurrentHashMap<String, Callback> CALLBACK_CACHE = new ConcurrentHashMap<>();

    public static void save(String messageId, Callback callback) {
        CALLBACK_CACHE.put(messageId, callback);
    }

    public static Callback get(String messageId) {
        return CALLBACK_CACHE.remove(messageId);
    }
}
