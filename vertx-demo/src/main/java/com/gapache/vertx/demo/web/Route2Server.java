package com.gapache.vertx.demo.web;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

/**
 * @author HuSen
 * @since 2021/2/24 6:01 下午
 */
public class Route2Server {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        Route route = router.route("/some/path");

        route.handler(ctx -> {
            HttpServerResponse response = ctx.response();
            // enable chunked responses because we will be adding data as
            // we execute over other handlers. This is only required once and
            // only if several handlers do output.
            response.setChunked(true);

            response.write("route1\n");

            ctx.vertx().setTimer(5000, tid -> ctx.next());
        });

        route.handler(ctx -> {

            HttpServerResponse response = ctx.response();
            response.write("route2\n");

            // Call the next matching route after a 5 second delay
            ctx.vertx().setTimer(5000, tid -> ctx.next());
        });

        route.handler(ctx -> {
            HttpServerResponse response = ctx.response();
            response.write("route3");

            // Now end the response
            ctx.response().end();
        });

        server.requestHandler(router).listen(8080);
    }
}
