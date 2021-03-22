package com.gapache.health.server;

import com.gapache.user.sdk.annotation.EnableUserServerFeign;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author HuSen
 * @date 2020/05/06 09:45 早上
 */
@EnableUserServerFeign
@SpringBootApplication
public class HealthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthServerApplication.class, args);
    }
}
