package com.gapache.job.common.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author HuSen
 * @since 2021/2/5 4:32 下午
 */
@Setter
@Getter
@ToString
public class JobVO implements Serializable {
    private static final long serialVersionUID = 1432637011546416386L;

    private Long id;

    private Long jobGroupId;

    private String name;

    private String cron;

    private String author;

    private JobStatus status;

    private String description;

    private String params;

    private BlockingStrategy blockingStrategy;

    private Integer retryTimes;

    private RouteStrategy routeStrategy;

    private String email;
}
