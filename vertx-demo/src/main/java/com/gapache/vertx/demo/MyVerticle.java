package com.gapache.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;

/**
 * 实现异步启动方法
 * 创建一个简单的HttpServer
 *
 * @author HuSen
 * @since 2021/2/24 1:52 下午
 */
public class MyVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        HttpServer server = vertx.createHttpServer().requestHandler(req -> {
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x!");
        });

        // 绑定服务
        server.listen(8080, res -> {
            if (res.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(res.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));

        MyVerticle verticle = new MyVerticle();

        vertx.deployVerticle(verticle);
    }
}
