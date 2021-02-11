package com.gapache.job.common.model;

import lombok.Data;

/**
 * @author HuSen
 * @since 2021/2/8 4:57 下午
 */
@Data
public class TriggerTaskRequest {

    private Long jobId;

    private String name;

    private String params;

    private String address;
}
