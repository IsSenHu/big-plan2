package com.gapache.job.common.model;

import lombok.Data;

/**
 * @author HuSen
 * @since 2021/2/5 9:40 上午
 */
@Data
public class JobInfo {

    private String name;

    private String author;

    private String cron;

    private String description;

    private JobStatus status;

    private BlockingStrategy blockingStrategy;

    private int retryTimes;

    private RouteStrategy routeStrategy;

    private String email;
}
