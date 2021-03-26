package com.gapache.user.server.dao.repository;

import com.gapache.jpa.BaseJpaRepository;
import com.gapache.user.server.dao.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * @author HuSen
 * @since 2020/9/8 11:29 上午
 */
public interface UserRepository extends BaseJpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    boolean existsByUsername(String username);

    UserEntity findByUsername(String username);

    @Query(nativeQuery = true,
            value = "SELECT tu.* FROM tb_user tu LEFT JOIN tb_user_relation tur ON tu.id = tur.user_id WHERE tur.owner_id = ?1")
    Page<UserEntity> findAllByOwnerId(Long ownerId, Pageable pageable);

    @Query(nativeQuery = true,
            value = "SELECT tu.* FROM tb_user tu LEFT JOIN tb_user_relation tur ON tu.id = tur.user_id WHERE tu.username like ?1 AND tur.owner_id = ?2")
    Page<UserEntity> findAllByOwnerIdAndUsernameLike(String username, Long ownerId, Pageable pageable);
}
