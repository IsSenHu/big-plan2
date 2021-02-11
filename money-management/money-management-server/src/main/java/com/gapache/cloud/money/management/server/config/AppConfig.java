package com.gapache.cloud.money.management.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author HuSen
 * @since 2021/1/21 1:32 下午
 */
@Data
@ConfigurationProperties(prefix = "com.gapache.app")
public class AppConfig {

    private Map<String, Config> configs;

    @Data
    public static class Config {
        public String name;
    }
}
