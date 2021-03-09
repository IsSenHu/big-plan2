package com.gapache.job.server.server.zeus;

import com.gapache.vertx.core.VertxManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

/**
 * @author HuSen
 * @since 2021/3/3 7:23 下午
 */
public class EventConsumerVerticle<T> extends AbstractVerticle {

    private final String address;
    private final Handler<Message<T>> handler;
    private EventBus eb;

    public EventConsumerVerticle(String address, Handler<Message<T>> handler) {
        this.address = address;
        this.handler = handler;
    }

    @Override
    public void start() {
        eb = VertxManager
                .checkNewStandalone()
                .eventBus();

        eb.consumer(address, handler);
    }

    public EventBus getEb() {
        return eb;
    }
}
