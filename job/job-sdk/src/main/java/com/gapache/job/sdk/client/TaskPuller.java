package com.gapache.job.sdk.client;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author HuSen
 * @since 2021/3/4 2:26 下午
 */
@Slf4j
public final class TaskPuller {

    private static final ConcurrentMap<String, LinkedBlockingQueue<Runnable>> QUEUE_MAP = new ConcurrentHashMap<>();

    public static void push(String name, Runnable taskExecutor) {
        LinkedBlockingQueue<Runnable> executorQueue = QUEUE_MAP.computeIfAbsent(name, key -> new LinkedBlockingQueue<>());
        if (executorQueue.offer(taskExecutor)) {
            if (log.isDebugEnabled()) {
                log.debug("push {} task in queue", name);
            }
        }
    }

    public static boolean isEmpty(String name) {
        return QUEUE_MAP.get(name) == null || QUEUE_MAP.get(name).isEmpty();
    }

    public static Runnable take(String name) {
        try {
            LinkedBlockingQueue<Runnable> executorQueue = QUEUE_MAP.get(name);
            if (executorQueue == null) {
                return null;
            }
            return executorQueue.take();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clear(String name) {
        LinkedBlockingQueue<Runnable> executorQueue = QUEUE_MAP.get(name);
        if (executorQueue != null) {
            executorQueue.clear();
        }
    }
}
