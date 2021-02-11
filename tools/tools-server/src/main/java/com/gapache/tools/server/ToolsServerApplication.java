package com.gapache.tools.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author HuSen
 * @since 2021/2/2 9:17 上午
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ToolsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolsServerApplication.class, args);
    }
}
