package com.gapache.vertx.web.test.pojo;

import com.gapache.vertx.web.zeus.DynamicServiceNameProvider;
import lombok.Setter;

/**
 * @author HuSen
 * @since 2021/3/3 5:49 下午
 */
@Setter
public class TestPoJo implements DynamicServiceNameProvider {

    private String serviceName;

    @Override
    public String getServiceName() {
        return this.serviceName;
    }
}
