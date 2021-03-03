package com.gapache.vertx.web.core;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author HuSen
 * @since 2021/3/1 3:01 下午
 */
@Configuration
@EnableConfigurationProperties(VertxSettings.class)
public class VertxManagerAutoConfiguration {

    public VertxManagerAutoConfiguration(VertxSettings vertxSettings) {
        new VertxManager(vertxSettings);
    }
}
