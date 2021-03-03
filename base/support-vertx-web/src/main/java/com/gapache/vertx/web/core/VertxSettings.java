package com.gapache.vertx.web.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author HuSen
 * @since 2021/3/1 2:56 下午
 */
@Data
@ConfigurationProperties(prefix = "com.gapache.vertx.settings")
public class VertxSettings {

    @Data
    @ConfigurationProperties(prefix = "com.gapache.vertx.web.settings")
    public static class HttpServer {
        private int port = 8080;
    }

    @Data
    @ConfigurationProperties(prefix = "com.gapache.vertx.http.client.settings")
    public static class HttpClient {

    }
}
