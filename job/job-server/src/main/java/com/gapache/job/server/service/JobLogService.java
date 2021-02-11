package com.gapache.job.server.service;

import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;
import com.gapache.job.common.model.JobLogVO;

/**
 * @author HuSen
 * @since 2021/2/4 4:45 下午
 */
public interface JobLogService {

    /**
     * 分页查询JobLog
     *
     * @param iPageRequest 分页参数
     * @return 分页结果
     */
    PageResult<JobLogVO> page(IPageRequest<JobLogVO> iPageRequest);
}
