package com.gapache.job.server.dao.repository;

import com.gapache.job.server.dao.entity.JobLogEntity;
import com.gapache.jpa.BaseJpaRepository;

/**
 * @author HuSen
 * @since 2021/2/4 4:44 下午
 */
public interface JobLogRepository extends BaseJpaRepository<JobLogEntity, Long> {

    /**
     * 根据messageId查询JobLog
     *
     * @param messageId messageId
     * @return JobLog
     */
    JobLogEntity findByMessageId(String messageId);
}
