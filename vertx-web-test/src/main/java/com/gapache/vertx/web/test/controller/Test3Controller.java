package com.gapache.vertx.web.test.controller;

import com.gapache.commons.model.JsonResult;
import com.gapache.vertx.web.annotation.RequestRouting;
import com.gapache.vertx.web.annotation.VertxController;
import com.gapache.vertx.web.test.client.Test3Client;
import com.gapache.vertx.web.test.pojo.TestPoJo;
import io.vertx.core.Future;

/**
 * @author HuSen
 * @since 2021/3/3 12:39 下午
 */
@VertxController
@RequestRouting("/test3")
public class Test3Controller implements Test3Client {

    @Override
    public Future<JsonResult<String>> hello(String name) {
        return Future.future(event -> event.complete(JsonResult.of("hello! " + name)));
    }

    @Override
    public Future<JsonResult<TestPoJo>> dynamic(TestPoJo poJo) {
        return Future.future(event -> event.complete(JsonResult.of(poJo)));
    }
}
