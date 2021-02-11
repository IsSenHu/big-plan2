package com.gapache.job.server.dao.repository;

import com.gapache.job.server.dao.entity.JobGroupEntity;
import com.gapache.jpa.BaseJpaRepository;

/**
 * @author HuSen
 * @since 2021/2/3 5:28 下午
 */
public interface JobGroupRepository extends BaseJpaRepository<JobGroupEntity, Long> {

    /**
     * 根据appName查询
     *
     * @param appName appName
     * @return JobGroup
     */
    JobGroupEntity findByAppName(String appName);
}
