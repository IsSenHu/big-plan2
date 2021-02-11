package com.gapache.job.server.dao.entity;

import com.gapache.job.common.model.BlockingStrategy;
import com.gapache.job.common.model.JobStatus;
import com.gapache.job.common.model.RouteStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @author HuSen
 * @since 2021/2/3 5:24 下午
 */
@Setter
@Getter
@ToString
@Entity
@Table(name = "tb_job")
public class JobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_group_id", nullable = false)
    private Long jobGroupId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cron", nullable = false)
    private String cron;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "params")
    private String params;

    @Column(name = "blocking_strategy", nullable = false)
    @Enumerated(EnumType.STRING)
    private BlockingStrategy blockingStrategy;

    @Column(name = "retry_times")
    private Integer retryTimes;

    @Column(name = "route_strategy", nullable = false)
    @Enumerated(EnumType.STRING)
    private RouteStrategy routeStrategy;

    @Column(name = "email")
    private String email;
}
