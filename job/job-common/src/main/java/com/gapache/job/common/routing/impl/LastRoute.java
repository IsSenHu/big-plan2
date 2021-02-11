package com.gapache.job.common.routing.impl;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.job.common.routing.Route;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Comparator;
import java.util.List;

/**
 * 最后一个：权重最小
 *
 * @author HuSen
 * @since 2021/2/8 10:46 上午
 */
public class LastRoute implements Route {

    @Override
    public Instance select(List<Instance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return null;
        }
        instances.sort(Comparator.comparingDouble(Instance::getWeight));
        return instances.get(0);
    }
}
