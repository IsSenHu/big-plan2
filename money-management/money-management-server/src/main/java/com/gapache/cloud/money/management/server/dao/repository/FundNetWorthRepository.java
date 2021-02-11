package com.gapache.cloud.money.management.server.dao.repository;

import com.gapache.cloud.money.management.server.dao.entity.FundNetWorthEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * @author HuSen
 * @since 2021/1/14 10:10 上午
 */
public interface FundNetWorthRepository extends JpaRepository<FundNetWorthEntity, Long> {

    List<FundNetWorthEntity> findAllByCodeAndDayIn(String code, Collection<LocalDate> dates);

    Page<FundNetWorthEntity> findAllByCode(String code, Pageable pageable);

    List<FundNetWorthEntity> findAllByCode(String code);

    List<FundNetWorthEntity> findAllByCodeAndDayGreaterThanEqual(String code, LocalDate start);

    List<FundNetWorthEntity> findAllByCodeAndDayLessThanEqual(String code, LocalDate end);

    List<FundNetWorthEntity> findAllByCodeAndDayGreaterThanEqualAndDayLessThanEqual(String code, LocalDate start, LocalDate end);
}
