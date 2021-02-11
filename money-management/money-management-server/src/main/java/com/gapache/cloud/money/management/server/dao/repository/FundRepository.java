package com.gapache.cloud.money.management.server.dao.repository;

import com.gapache.cloud.money.management.server.dao.entity.FundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

/**
 * @author HuSen
 * @since 2021/1/13 2:10 下午
 */
public interface FundRepository extends JpaRepository<FundEntity, Long>, JpaSpecificationExecutor<FundEntity> {

    List<FundEntity> findAllByCodeIn(Collection<String> codes);

    FundEntity findByCode(String code);

    List<FundEntity> findAllByHold(Boolean hold);

    List<FundEntity> findAllByOptional(Boolean optional);
}
