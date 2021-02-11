package com.gapache.job.sdk.annotation;

import com.gapache.job.common.model.BlockingStrategy;
import com.gapache.job.common.model.JobStatus;
import com.gapache.job.common.model.RouteStrategy;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author HuSen
 * @since 2021/2/5 9:30 上午
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ZzhJob {

    String name();

    String author();

    String cron();

    String description();

    /**
     * @return 任务默认的状态
     */
    JobStatus status() default JobStatus.STOP;

    /**
     * @return 阻塞策略
     */
    BlockingStrategy blockingStrategy() default BlockingStrategy.STAND_ALONE_SERIAL;

    /**
     * @return 失败重试次数 0表示不重试
     */
    int retryTimes() default 0;

    /**
     * @return 路由策略
     */
    RouteStrategy routeStrategy() default RouteStrategy.NACOS;

    /**
     * @return 失败告警邮箱，默认是发件人自己
     */
    String email() default "";
}
