package com.gapache.job.sdk;

import com.gapache.job.common.model.TaskInfo;
import com.gapache.job.common.model.TaskResult;

/**
 * @author HuSen
 * @since 2021/2/5 9:34 上午
 */
public interface JobTrigger {

    /**
     * 调度任务的接口
     * 由子类实现
     *
     * @param taskInfo   任务信息
     * @return 执行成功
     */
    boolean execute(TaskInfo taskInfo);
}
