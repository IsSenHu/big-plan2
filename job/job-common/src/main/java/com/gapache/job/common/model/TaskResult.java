package com.gapache.job.common.model;

import com.dyuproject.protostuff.Tag;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author HuSen
 * @since 2021/2/5 9:11 上午
 */
@Data
public class TaskResult {

    public static final int SUCCESS = 0;

    @Tag(1)
    private int code;

    @Tag(2)
    private String remark;

    @Tag(3)
    private String messageId;

    @Tag(4)
    private LocalDateTime executorTime;
}
