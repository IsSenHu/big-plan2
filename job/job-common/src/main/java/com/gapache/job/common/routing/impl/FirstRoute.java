package com.gapache.job.common.routing.impl;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.job.common.routing.Route;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Comparator;
import java.util.List;

/**
 * 当有任务待执行器执行时，调度中心会将任务下发到权重最高的执行器。
 *
 * @author HuSen
 * @since 2021/2/8 10:43 上午
 */
public class FirstRoute implements Route {

    @Override
    public Instance select(List<Instance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return null;
        }
        instances.sort(Comparator.comparingDouble(Instance::getWeight));
        return instances.get(instances.size() - 1);
    }
}
