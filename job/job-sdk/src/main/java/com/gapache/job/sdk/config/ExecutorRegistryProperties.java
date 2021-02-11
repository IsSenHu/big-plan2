package com.gapache.job.sdk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author HuSen
 * @since 2021/2/4 11:01 上午
 */
@Data
@ConfigurationProperties(prefix = "com.gapache.job.executor")
public class ExecutorRegistryProperties {

    /**
     * 描述
     */
    private String name;

    private String toServerPrivateKey;

    private String fromServerPublicKey;

    private int localPort;
}
