package com.gapache.job.sdk.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author HuSen
 * @since 2021/2/3 6:00 下午
 */
@Configuration
@EnableConfigurationProperties(InnerServerProperties.class)
public class InnerServerAutoConfiguration {

}
