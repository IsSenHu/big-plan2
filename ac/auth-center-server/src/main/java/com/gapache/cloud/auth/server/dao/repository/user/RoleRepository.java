package com.gapache.cloud.auth.server.dao.repository.user;

import com.gapache.cloud.auth.server.dao.entity.RoleEntity;
import com.gapache.jpa.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author HuSen
 * @since 2021/1/26 9:47 上午
 */
public interface RoleRepository extends BaseJpaRepository<RoleEntity, Long> {

    RoleEntity findByName(String name);

    List<RoleEntity> findAllByNameLike(String name);

    List<RoleEntity> findAllByGroupAndIdNotAndNameLike(Long group, Long roleId, String name);

    @Query("FROM RoleEntity R WHERE R.id = (SELECT UR.roleId FROM UserRoleEntity UR WHERE UR.userId = ?1)")
    RoleEntity findByUserId(Long userId);

    Page<RoleEntity> findAllByGroupAndIdNot(Long group, Long roleId);

    List<RoleEntity> findAllByGroupAndIdIsNot(Long group, Long roleId);
}
