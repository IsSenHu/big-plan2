package com.gapache.vertx.demo.web;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * @author HuSen
 * @since 2021/2/24 7:20 下午
 */
public class Route3Server {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer(new HttpServerOptions().setLogActivity(true));

        Router router = Router.router(vertx);

        router.get("/some/path")
                .respond(ctx -> Future.succeededFuture(new JsonObject().put("hello", "world")));

        router.get("/some/path2")
                .respond(ctx -> Future.succeededFuture(new JsonObject().put("name", "胡森")));

        httpServer
                .requestHandler(router)
                .exceptionHandler(System.out::println)
                .listen(8080);
    }
}
