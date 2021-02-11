package com.gapache.job.server.dao.repository;

import com.gapache.job.server.dao.entity.JobEntity;
import com.gapache.jpa.BaseJpaRepository;

import java.util.List;

/**
 * @author HuSen
 * @since 2021/2/3 5:29 下午
 */
public interface JobRepository extends BaseJpaRepository<JobEntity, Long> {

    /**
     * 根据组查询任务
     *
     * @param jobGroupId jobGroupId
     * @return 任务
     */
    List<JobEntity> findAllByJobGroupId(Long jobGroupId);
}
