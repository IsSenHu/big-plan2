package com.gapache.job.sdk.config;

import com.gapache.job.sdk.client.zeus.server.JobServerClient;
import com.gapache.job.sdk.registry.RegistryEvent;
import com.gapache.vertx.web.annotation.EnableVertxWeb;
import com.gapache.vertx.web.annotation.EnableZeusClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.UUID;

/**
 * @author HuSen
 * @since 2021/3/4 4:48 下午
 */
@Configuration
@ConditionalOnProperty(value = "com.gapache.job.executor.rpc", havingValue = "zeus")
@EnableVertxWeb
@EnableZeusClients(basePackageClasses = JobServerClient.class)
@ComponentScan(basePackages = "com.gapache.job.sdk.client.zeus.impl")
public class ZeusRpcModeAutoConfiguration {

    @EventListener(ApplicationStartedEvent.class)
    public void onMessage(ApplicationStartedEvent event) {
        String port = event.getApplicationContext().getEnvironment().getProperty("com.gapache.vertx.web.settings.port");
        if (port != null) {
            event.getApplicationContext().publishEvent(new RegistryEvent(UUID.randomUUID().toString(), null, Integer.parseInt(port)));
        }
    }
}
