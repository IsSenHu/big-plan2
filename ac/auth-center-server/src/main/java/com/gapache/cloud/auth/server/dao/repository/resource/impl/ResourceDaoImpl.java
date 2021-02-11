package com.gapache.cloud.auth.server.dao.repository.resource.impl;

import com.gapache.cloud.auth.server.dao.entity.ResourceEntity;
import com.gapache.cloud.auth.server.dao.repository.resource.ResourceDao;
import com.gapache.commons.utils.IStringUtils;
import com.gapache.jpa.BaseJpaRepositoryBean;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HuSen
 * @since 2021/1/26 4:55 下午
 */
@Slf4j
public class ResourceDaoImpl extends BaseJpaRepositoryBean<ResourceEntity, Long> implements ResourceDao {

    private final Map<String, String> sqlMap = new HashMap<>(4);

    public ResourceDaoImpl(EntityManager entityManager) {
        super(ResourceEntity.class, entityManager);
    }

    @Override
    public List<ResourceEntity> findAllResource(Long userId) {
        String sql = sqlMap.get("FindAllResourceByRoleId");
        if (StringUtils.isBlank(sql)) {
            try {
                byte[] bytes = FileCopyUtils.copyToByteArray(new ClassPathResource("sql/FindAllResourceByUserId.sql").getInputStream());
                sql = IStringUtils.newString(bytes);
                sqlMap.put("FindAllResourceByRoleId", sql);
            } catch (IOException e) {
                log.error("findAllResource error:{}", userId, e);
                return Lists.newArrayList();
            }
        }
        Query query = em.createNativeQuery("SELECT\n" +
                "       tr.id AS id,\n" +
                "       tr.resource_server_id AS resource_server_id,\n" +
                "       tr.resource_server_name AS resource_server_name,\n" +
                "       tr.scope AS scope,\n" +
                "       tr.name AS  name\n" +
                "FROM tb_resource tr", ResourceEntity.class);
//        query.setParameter("userId", userId);
        for (Map.Entry<String, Object> hint : query.getHints().entrySet()) {
            System.out.println(hint);
            query.setHint(hint.getKey(), hint.getValue());
        }
        System.out.println(query.getResultList());
        // 这个就必须加注解才能进行自动映射
        return null;
    }

    @Override
    public ResourceEntity findResourceCustomizeById(Long id) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", 1L);
        Query nativeQuery = com.gapache.cloud.auth.server.dao.sql.Query.FIND_ALL_RESOURCE
                .build(em, params);

        return (ResourceEntity) nativeQuery.getSingleResult();
    }
}
