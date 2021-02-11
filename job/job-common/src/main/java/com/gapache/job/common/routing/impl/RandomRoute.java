package com.gapache.job.common.routing.impl;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.job.common.routing.Route;
import org.apache.commons.collections4.CollectionUtils;

import java.security.SecureRandom;
import java.util.List;

/**
 * 随机路由策略
 *
 * @author HuSen
 * @since 2021/2/8 10:21 上午
 */
public class RandomRoute implements Route {

    private final SecureRandom random = new SecureRandom();

    @Override
    public Instance select(List<Instance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return null;
        }
        return instances.get(random.nextInt(instances.size()));
    }
}
