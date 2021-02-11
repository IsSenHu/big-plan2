package com.gapache.job.common.model;

import com.dyuproject.protostuff.Tag;
import lombok.Data;

/**
 * @author HuSen
 * @since 2021/2/4 4:34 下午
 */
@Data
public class TaskInfo {

    @Tag(1)
    private String name;

    @Tag(2)
    private String params;

    @Tag(3)
    private BlockingStrategy blockingStrategy;
}
