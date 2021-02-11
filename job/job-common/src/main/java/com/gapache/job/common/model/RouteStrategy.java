package com.gapache.job.common.model;

import com.gapache.job.common.routing.Route;
import com.gapache.job.common.routing.impl.FirstRoute;
import com.gapache.job.common.routing.impl.LastRoute;
import com.gapache.job.common.routing.impl.RandomRoute;
import com.gapache.job.common.routing.impl.RoundRobinRoute;

/**
 * 路由策略
 *
 * @author HuSen
 * @since 2021/2/8 10:52 上午
 */
public enum RouteStrategy {
    //
    FIRST(new FirstRoute(), "第一个"),
    LAST(new LastRoute(), "最后一个"),
    RANDOM(new RandomRoute(), "随机"),
    ROUND_ROBIN(new RoundRobinRoute(), "轮询"),
    BROADCAST(null, "广播模式"),
    NACOS(null, "使用NACOS选举");

    /**
     * 路由策略
     */
    private final Route route;
    private final String name;

    RouteStrategy(Route route, String name) {
        this.route = route;
        this.name = name;
    }

    public Route getRoute() {
        return route;
    }

    public String getName() {
        return name;
    }
}
