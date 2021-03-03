package com.gapache.vertx.web.core;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * @author HuSen
 * @since 2021/3/1 2:55 下午
 */
@Slf4j
public class VertxManager {

    private final Vertx vertx;
    private final VertxSettings settings;
    private static VertxManager instance;

    protected VertxManager(VertxSettings settings) {
        // TODO apply settings
        this.settings = settings;
        this.vertx = Vertx.vertx();
        VertxManager.instance = this;
    }

    public static Vertx getVertx() {
        if (instance == null) {
            return createNew();
        }
        return instance.vertx;
    }

    public static Vertx createNew() {
        log.warn("create a new default vertx");
        return Vertx.vertx();
    }

    public static VertxSettings getSettings() {
        return instance.settings;
    }
}
