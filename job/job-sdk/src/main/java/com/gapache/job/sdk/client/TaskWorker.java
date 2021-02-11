package com.gapache.job.sdk.client;

import com.gapache.job.common.thread.JobThreadBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author HuSen
 * @since 2021/2/7 2:11 下午
 */
@Slf4j
public class TaskWorker implements Runnable {

    private final ThreadPoolExecutor bizExecutor;

    private final List<String> names;

    public TaskWorker(List<String> names) {
        this.bizExecutor = new ThreadPoolExecutor(
                names.size(),
                names.size(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000),
                r -> new Thread(r, "job-rpc, InnerClient bizThreadPool-" + r.hashCode()),
                (r, executor) -> {
                    throw new RuntimeException("job, InnerClient bizThreadPool is EXHAUSTED!");
                });
        this.names = names;
    }

    @Override
    public void run() {
        names.forEach(name -> {
            Thread thread = new JobThreadBuilder()
                    .create(() -> {
                        log.info("create task thread {}", name);
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                ServerMessageHandler.TaskExecutor taskExecutor = ServerMessageHandler.TaskPuller.take(name);
                                if (taskExecutor != null) {
                                    taskExecutor.run();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .build();

            bizExecutor.execute(thread);
        });
    }
}
