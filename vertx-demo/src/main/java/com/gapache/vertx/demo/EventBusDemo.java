package com.gapache.vertx.demo;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

/**
 * @author HuSen
 * @since 2021/2/24 2:07 下午
 */
public class EventBusDemo {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        EventBus eb = vertx.eventBus();

        eb.consumer("news.uk.sport", message -> System.out.println("I have received a message: " + message.body()));
        
        eb.publish("news.uk.sport", "Yay! Someone kicked a ball");

        eb.consumer("news.uk.sport", message -> System.out.println("I have received a message2: " + message.body()));

        eb.send("news.uk.sport", "Yay! Someone kicked a ball");
        eb.send("news.uk.sport", "Yay! Someone kicked a ball");
        eb.send("news.uk.sport", "Yay! Someone kicked a ball");
        eb.send("news.uk.sport", "Yay! Someone kicked a ball");
    }
}
