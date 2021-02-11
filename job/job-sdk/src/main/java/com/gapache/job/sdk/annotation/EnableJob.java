package com.gapache.job.sdk.annotation;

import java.lang.annotation.*;

/**
 * @author HuSen
 * @since 2021/2/5 1:22 下午
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableJob {
}
