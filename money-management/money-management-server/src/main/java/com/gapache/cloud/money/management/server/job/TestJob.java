//package com.gapache.cloud.money.management.server.job;
//
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
// * @since 2021/2/5 1:28 下午
// */
//@Slf4j
//@ZzhJob(name = "test", author = "胡森", cron = "0/5 * * * * ? *", description = "测试", status = JobStatus.RUNNING)
//public class TestJob implements JobTrigger {
//
//    private final AtomicInteger atomicInteger = new AtomicInteger(20);
//
//    @Override
//    public boolean execute(TaskInfo taskInfo) {
//        log.debug(">>>>>> task {} {} {}", taskInfo.getBlockingStrategy(), taskInfo, atomicInteger.get());
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
