package com.gapache.job.common.thread;

/**
 * @author HuSen
 * @since 2021/2/4 9:15 上午
 */
public class JobThreadBuilder {

    private static final JobThreadFactory THREAD_FACTORY = new JobThreadFactory();

    private Thread thread;

    public JobThreadBuilder create(Runnable runnable) {
        this.thread = THREAD_FACTORY.newThread(runnable);
        return this;
    }

    public JobThreadBuilder naming(String name) {
        this.thread.setName(name);
        return this;
    }

    public JobThreadBuilder daemon(boolean daemon) {
        this.thread.setDaemon(daemon);
        return this;
    }

    public Thread buildAndStart() {
        this.thread.start();
        return this.thread;
    }

    public Thread build() {
        return this.thread;
    }
}
