package com.gapache.job.server.dao.entity;

import com.gapache.vertx.redis.annotation.Id;
import com.gapache.vertx.redis.annotation.RedisEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author HuSen
 * @since 2021/2/4 4:44 下午
 */
@Getter
@Setter
@ToString
@RedisEntity("JOB_LOG")
public class JobLogEntity {

    @Id
    private String id;

    private Long jobId;

    private LocalDateTime triggerTime;

    private Boolean triggerResult;

    private String triggerRemark;

    private LocalDateTime executorTime;

    private Boolean executorResult;

    private String executorRemark;

    private int retryTimes;

    private String params;

    public static JobLogEntity of(Long jobId) {
        JobLogEntity jobLog = new JobLogEntity();
        jobLog.setJobId(jobId);
        jobLog.setTriggerTime(LocalDateTime.now());
        jobLog.setTriggerResult(false);
        return jobLog;
    }
}
