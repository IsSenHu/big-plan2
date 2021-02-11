package com.gapache.job.server.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author HuSen
 * @since 2021/2/4 4:44 下午
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "tb_job_log")
public class JobLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "trigger_time", nullable = false)
    private LocalDateTime triggerTime;

    @Column(name = "trigger_result", nullable = false)
    private Boolean triggerResult;

    @Column(name = "trigger_remark")
    private String triggerRemark;

    @Column(name = "executor_time")
    private LocalDateTime executorTime;

    @Column(name = "executor_result")
    private Boolean executorResult;

    @Column(name = "executor_remark")
    private String executorRemark;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "retry_times")
    private int retryTimes;

    @Column(name = "params")
    private String params;
}
