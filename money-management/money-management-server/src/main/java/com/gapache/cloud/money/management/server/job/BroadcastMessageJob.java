package com.gapache.cloud.money.management.server.job;

import com.gapache.job.common.model.JobStatus;
import com.gapache.job.common.model.RouteStrategy;
import com.gapache.job.common.model.TaskInfo;
import com.gapache.job.sdk.JobTrigger;
import com.gapache.job.sdk.annotation.ZzhJob;
import lombok.extern.slf4j.Slf4j;

/**
 * @author HuSen
 * @since 2021/2/8 1:47 下午
 */
@ZzhJob(
        name = "BroadcastMessageJob",
        author = "胡森",
        cron = "0/30 * * * * ? *",
        description = "广播任务测试",
        status = JobStatus.RUNNING,
        routeStrategy = RouteStrategy.BROADCAST
)
@Slf4j
public class BroadcastMessageJob implements JobTrigger {

    @Override
    public boolean execute(TaskInfo taskInfo) {
        log.info("BroadcastMessageJob......");
        return true;
    }
}
