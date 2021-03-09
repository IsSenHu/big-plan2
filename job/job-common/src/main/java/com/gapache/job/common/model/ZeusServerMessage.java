package com.gapache.job.common.model;

import com.gapache.vertx.web.zeus.DynamicServiceNameProvider;
import lombok.Getter;
import lombok.Setter;

/**
 * 来自Zeus的ServerMessage
 *
 * @author HuSen
 * @since 2021/3/3 5:59 下午
 */
@Setter
@Getter
public class ZeusServerMessage implements DynamicServiceNameProvider {
    private String serviceName;
    private ServerMessage message;
    private String ip;
    private int port;

    @Override
    public String ip() {
        return ip;
    }

    @Override
    public int port() {
        return port;
    }
}
