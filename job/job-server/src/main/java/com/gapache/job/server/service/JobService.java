package com.gapache.job.server.service;

import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;
import com.gapache.job.common.model.JobVO;
import com.gapache.job.common.model.TriggerTaskRequest;

/**
 * @author HuSen
 * @since 2021/2/4 4:45 下午
 */
public interface JobService {

    /**
     * 分页查询Job
     *
     * @param iPageRequest 分页参数
     * @return 分页结果
     */
    PageResult<JobVO> page(IPageRequest<JobVO> iPageRequest);

    /**
     * 修改job状态
     *
     * @param vo VO
     * @return 是否修改成功
     */
    Boolean changeStatus(JobVO vo);

    /**
     * 修改任务调度时间
     *
     * @param vo VO
     * @return 是否修改成功
     */
    Boolean changeCron(JobVO vo);

    /**
     * 手动调度任务
     *
     * @param request TriggerTaskRequest
     * @return 调度是否成功
     */
    Boolean trigger(TriggerTaskRequest request);
}
