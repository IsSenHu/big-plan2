package com.gapache.cloud.money.management.server;

import com.gapache.cloud.money.management.common.model.FundDTO;
import com.gapache.cloud.money.management.server.config.AppConfig;
import com.gapache.job.sdk.annotation.EnableJob;
import com.gapache.security.annotation.EnableAuthResourceServer;
import com.gapache.web.EnableCommonAdvice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author HuSen
 * @since 2020/8/10 11:14 上午
 */
@EnableDiscoveryClient
@EnableScheduling
@EnableAuthResourceServer("MoneyManagement")
@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
@EnableCommonAdvice
@EnableJob
public class MoneyManagementServer {

    public static void main(String[] args) {
        SpringApplication.run(MoneyManagementServer.class, args);
    }

    @Bean
    @ConditionalOnProperty(prefix = "com.gapache.app", value = "configs")
    public FundDTO fundDTO(AppConfig appConfig) {
        System.out.println(appConfig);
        return new FundDTO();
    }
}
