//package com.gapache.cloud.money.management.server.job;
//
//import com.gapache.job.common.model.BlockingStrategy;
//import com.gapache.job.common.model.JobStatus;
//import com.gapache.job.common.model.TaskInfo;
//import com.gapache.job.sdk.JobTrigger;
//import com.gapache.job.sdk.annotation.ZzhJob;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * @author HuSen
// * @since 2021/2/7 1:40 下午
// */
//@Slf4j
//@ZzhJob(name = "test3", author = "胡森", cron = "0/5 * * * * ? *", description = "测试3", status = JobStatus.RUNNING, blockingStrategy = BlockingStrategy.COVER)
//public class TestJob3 implements JobTrigger {
//
//    private final AtomicInteger atomicInteger = new AtomicInteger(20);
//
//    @Override
//    public boolean execute(TaskInfo taskInfo) {
//        log.debug(">>>>>> task3 {} {} {}", taskInfo.getBlockingStrategy(), taskInfo, atomicInteger.get());
//        try {
//            if (atomicInteger.get() > 1) {
//                TimeUnit.SECONDS.sleep(atomicInteger.getAndDecrement());
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return true;
//    }
//}
