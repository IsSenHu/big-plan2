package com.gapache.job.common.thread;

import java.util.concurrent.ThreadFactory;

/**
 * @author HuSen
 * @since 2021/2/3 5:30 下午
 */
public class JobThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "job >>>>>>");
    }
}
