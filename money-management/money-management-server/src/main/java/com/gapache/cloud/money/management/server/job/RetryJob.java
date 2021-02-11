//package com.gapache.cloud.money.management.server.job;//package com.gapache.cloud.money.management.server.job;
//
//import com.gapache.job.common.model.JobStatus;
//import com.gapache.job.common.model.TaskInfo;
//import com.gapache.job.sdk.JobTrigger;
//import com.gapache.job.sdk.annotation.ZzhJob;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * @author HuSen
// * @since 2021/2/5 1:28 下午
// */
//@Slf4j
//@ZzhJob(
//        name = "RetryJob",
//        author = "胡森",
//        cron = "0/10 * * * * ? *",
//        description = "测试重试次数的job",
//        status = JobStatus.RUNNING,
//        retryTimes = 1
//)
//public class RetryJob implements JobTrigger {
//
//    private final AtomicInteger atomicInteger = new AtomicInteger(0);
//
//    @Override
//    public boolean execute(TaskInfo taskInfo) {
//        log.info(">>>>>> task {} {}", taskInfo.getBlockingStrategy(), taskInfo);
//        atomicInteger.incrementAndGet();
//        return atomicInteger.get() > 1;
//    }
//}
