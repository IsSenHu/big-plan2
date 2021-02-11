package com.gapache.job.common.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author HuSen
 * @since 2021/2/5 4:35 下午
 */
@Setter
@Getter
@ToString
public class JobLogVO implements Serializable {
    private static final long serialVersionUID = 1299908309619820097L;

    private Long id;

    private Long jobId;

    private LocalDateTime triggerTime;

    private Boolean triggerResult;

    private String triggerRemark;

    private LocalDateTime executorTime;

    private Boolean executorResult;

    private String executorRemark;

    private String messageId;
}
