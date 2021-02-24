package com.gapache.vertx.demo.web;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;

/**
 * @author HuSen
 * @since 2021/2/24 5:33 下午
 */
public class HelloWorld {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        HttpServer server = vertx.createHttpServer();

        server.requestHandler(request -> {

            // This handler gets called for each request that arrives on the server
            HttpServerResponse response = request.response();
            response.putHeader("content-type", "text/plain");

            // Write to the response and end it
            response.end("Hello World!");
        });

        server.listen(8080);
    }
}
