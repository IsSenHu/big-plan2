package com.gapache.job.server.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author HuSen
 * @since 2021/2/3 5:57 下午
 */
@Data
@ConfigurationProperties(prefix = "com.gapache.job.server")
public class InnerServerProperties {

    private Integer port;

    private String toExecutorPrivateKey;

    private String fromExecutorPublicKey;

    private String rpc = "zeus";
}
