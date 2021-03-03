package com.gapache.vertx.web.zeus;

import com.netflix.loadbalancer.ILoadBalancer;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Map;

/**
 * @author HuSen
 * @since 2021/3/3 10:36 上午
 */
public class CachingSpringLoadBalancerFactory {

    protected final SpringClientFactory factory;

    private final Map<String, ILoadBalancer> cache = new ConcurrentReferenceHashMap<>();

    public CachingSpringLoadBalancerFactory(SpringClientFactory factory) {
        this.factory = factory;
    }

    public ILoadBalancer create(String clientName) {
        ILoadBalancer client = this.cache.get(clientName);
        if (client != null) {
            return client;
        }
        client = factory.getLoadBalancer(clientName);
        this.cache.put(clientName, client);
        return client;
    }

    public SpringClientFactory getFactory() {
        return factory;
    }
}
