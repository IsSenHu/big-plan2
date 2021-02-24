package com.gapache.vertx.demo;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author HuSen
 * @since 2021/2/22 1:33 下午
 */
public class CompletionStageDemo {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Future<String> future = vertx.createDnsClient().lookup("vertx.io");
        future.toCompletionStage().whenComplete((ip, err) -> {
            if (err != null) {
                System.err.println("Could not resolve vertx.io");
                err.printStackTrace();
            } else {
                System.out.println("vertx.io => " + ip);
            }
        });
    }
}
