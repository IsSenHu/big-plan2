package com.gapache.job.server.server.config;

import com.gapache.job.sdk.client.zeus.client.ExecutorClient;
import com.gapache.job.server.server.InnerServer;
import com.gapache.job.server.server.NettyServerMessageSender;
import com.gapache.job.server.server.ServerMessageSender;
import com.gapache.job.server.server.zeus.ZeusServerMessageSender;
import com.gapache.vertx.web.annotation.EnableVertxWeb;
import com.gapache.vertx.web.annotation.EnableZeusClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author HuSen
 * @since 2021/2/3 6:00 下午
 */
@Configuration
@EnableConfigurationProperties(InnerServerProperties.class)
public class InnerServerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(value = "com.gapache.job.server.rpc", havingValue = "netty")
    public InnerServer innerServer(InnerServerProperties innerServerProperties) {
        return new InnerServer(innerServerProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "com.gapache.job.server.rpc", havingValue = "netty")
    public ServerMessageSender serverMessageSenderByNetty() {
        return new NettyServerMessageSender();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "com.gapache.job.server.rpc", havingValue = "zeus")
    public ServerMessageSender serverMessageSenderByZeus(ApplicationContext applicationContext) {
        return new ZeusServerMessageSender(applicationContext);
    }

    @Configuration
    @ConditionalOnProperty(value = "com.gapache.job.server.rpc", havingValue = "zeus")
    @EnableVertxWeb
    @EnableZeusClients(basePackageClasses = ExecutorClient.class)
    public static class ZeusModeSwitch {

    }
}
