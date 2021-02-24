package com.gapache.vertx.demo.web;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

/**
 * @author HuSen
 * @since 2021/2/24 5:36 下午
 */
public class Route1Server {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.route().handler(ctx -> {

            // This handler will be called for every request
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type", "text/plain");

            // Write to the response and end it
            response.end("Hello World from Vert.x-Web!");
        });

        server.requestHandler(router).listen(8080);
    }
}
