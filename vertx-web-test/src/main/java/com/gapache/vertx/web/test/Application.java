package com.gapache.vertx.web.test;

import com.gapache.vertx.web.annotation.EnableZeusClients;
import com.gapache.vertx.web.test.client.P;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author HuSen
 * @since 2021/3/2 9:55 上午
 */
@EnableDiscoveryClient
@EnableZeusClients(basePackageClasses = P.class)
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
