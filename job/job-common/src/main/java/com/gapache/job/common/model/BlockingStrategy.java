package com.gapache.job.common.model;

/**
 * 阻塞策略
 *
 * @author HuSen
 * @since 2021/2/5 5:08 下午
 */
public enum BlockingStrategy {
    // 单机串行
    STAND_ALONE_SERIAL,
    // 丢弃后续调度
    DISCARD,
    // 覆盖之前调度
    COVER
}
