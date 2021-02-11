package com.gapache.job.server.service.impl;

import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;
import com.gapache.job.common.model.JobLogVO;
import com.gapache.job.server.dao.entity.JobLogEntity;
import com.gapache.job.server.dao.repository.JobLogRepository;
import com.gapache.job.server.service.JobLogService;
import com.gapache.jpa.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HuSen
 * @since 2021/2/4 4:45 下午
 */
@Service
public class JobLogServiceImpl implements JobLogService {

    @Resource
    private JobLogRepository jobLogRepository;

    @Override
    public PageResult<JobLogVO> page(IPageRequest<JobLogVO> iPageRequest) {
        Pageable pageable = PageHelper.of(iPageRequest);
        Page<JobLogEntity> page = jobLogRepository.findAll(pageable);
        return PageResult.of(page.getTotalElements(), po -> {
            JobLogVO vo = new JobLogVO();
            BeanUtils.copyProperties(po, vo);
            return vo;
        }, page.getContent());
    }
}
