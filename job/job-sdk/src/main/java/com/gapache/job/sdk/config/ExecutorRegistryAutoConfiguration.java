package com.gapache.job.sdk.config;

import com.gapache.job.sdk.JobServerChecker;
import com.gapache.job.sdk.annotation.EnableJob;
import com.gapache.job.sdk.client.InnerClient;
import com.gapache.job.sdk.registry.ExecutorRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author HuSen
 * @since 2021/2/4 10:58 上午
 */
@Configuration
@EnableConfigurationProperties(ExecutorRegistryProperties.class)
@ConditionalOnBean(annotation = EnableJob.class)
@EnableScheduling
public class ExecutorRegistryAutoConfiguration {

    @Bean
    public ExecutorRegistry executorRegistry() {
        return new ExecutorRegistry();
    }

    @Bean
    public InnerClient innerClient(ExecutorRegistryProperties properties) {
        return new InnerClient(properties);
    }

    @Bean
    public JobServerChecker jobServerChecker() {
        return new JobServerChecker();
    }
}
