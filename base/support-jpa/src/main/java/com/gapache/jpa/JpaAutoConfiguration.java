package com.gapache.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

/**
 * @author HuSen
 * create on 2020/4/29 3:47 下午
 */
public class JpaAutoConfiguration {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return new EntityAuditorAware();
    }
}
