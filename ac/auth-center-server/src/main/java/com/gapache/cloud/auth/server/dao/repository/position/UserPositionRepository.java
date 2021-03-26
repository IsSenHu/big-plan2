package com.gapache.cloud.auth.server.dao.repository.position;

import com.gapache.cloud.auth.server.dao.entity.UserPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author HuSen
 * @since 2021/3/26 2:18 下午
 */
public interface UserPositionRepository extends JpaRepository<UserPositionEntity, Long> {

    List<UserPositionEntity> findAllByUserId(Long userId);

    List<UserPositionEntity> findAllByPositionId(Long positionId);

    List<UserPositionEntity> findAllByPositionIdIn(Collection<Long> positionIds);
}
