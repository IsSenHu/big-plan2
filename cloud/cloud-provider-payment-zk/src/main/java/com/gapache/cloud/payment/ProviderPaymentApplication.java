package com.gapache.cloud.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * zookeeper创建的服务实例节点是临时的
 * CAP中的CP
 *
 * @author HuSen
 * @since 2020/6/2 3:33 下午
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ProviderPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderPaymentApplication.class);
    }
}
