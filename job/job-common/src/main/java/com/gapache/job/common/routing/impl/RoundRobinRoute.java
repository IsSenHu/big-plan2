package com.gapache.job.common.routing.impl;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gapache.job.common.routing.Route;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询
 *
 * @author HuSen
 * @since 2021/2/8 10:35 上午
 */
public class RoundRobinRoute implements Route {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public Instance select(List<Instance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return null;
        }
        int current;
        int next;
        do {
            current = atomicInteger.get();
            next = current == Integer.MAX_VALUE ? 0 : current + 1;
        } while (!atomicInteger.compareAndSet(current, next));

        return instances.get(current % instances.size());
    }
}
