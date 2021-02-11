package com.gapache.cloud.money.management.server.job;

import com.gapache.job.common.model.JobStatus;
import com.gapache.job.common.model.TaskInfo;
import com.gapache.job.sdk.JobTrigger;
import com.gapache.job.sdk.annotation.ZzhJob;

/**
 * @author HuSen
 * @since 2021/2/8 3:09 下午
 */
@ZzhJob(
        name = "EmailJob",
        author = "胡森",
        cron = "0/50 * * * * ? *",
        description = "测试邮件告警功能",
        status = JobStatus.RUNNING,
        retryTimes = 1,
        email = "husen@51ishare.com"
)
public class EmailJob implements JobTrigger {

    @Override
    public boolean execute(TaskInfo taskInfo) {
        return true;
    }
}
