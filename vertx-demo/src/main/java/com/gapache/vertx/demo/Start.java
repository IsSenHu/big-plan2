package com.gapache.vertx.demo;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * @author HuSen
 * @since 2021/2/22 11:26 上午
 */
public class Start {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));

        MyVerticle verticle = new MyVerticle();

        vertx.deployVerticle(verticle);
    }
}
