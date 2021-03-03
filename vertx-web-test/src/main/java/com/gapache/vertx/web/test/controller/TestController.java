package com.gapache.vertx.web.test.controller;

import com.gapache.commons.model.JsonResult;
import com.gapache.vertx.web.annotation.GetRouting;
import com.gapache.vertx.web.annotation.PostRouting;
import com.gapache.vertx.web.annotation.RequestRouting;
import com.gapache.vertx.web.annotation.VertxController;
import com.gapache.vertx.web.test.client.Test2Client;
import com.gapache.vertx.web.test.client.Test4Client;

import javax.annotation.Resource;

/**
 * @author HuSen
 * @since 2021/3/2 10:18 上午
 */
@VertxController
@RequestRouting("/test")
public class TestController {

    @Resource
    private Test2Client test2Client;

    @Resource
    private Test4Client test3Client;

    @GetRouting
    public JsonResult<String> test() {
        test2Client.test2("胡森", 25).onComplete(event -> {
            if (event.succeeded()) {
                System.out.println(event.result());
            } else {
                event.cause().printStackTrace();
            }
        });

        test3Client.hello("胡森").onComplete(event -> {
            if (event.succeeded()) {
                System.out.println(event.result());
            } else {
                event.cause().printStackTrace();
            }
        });
        return JsonResult.of("hello,world");
    }

    @PostRouting("/test2")
    public JsonResult<String> test2(String name, Integer age) {
        System.out.println(name + ": " + age);
        return JsonResult.of(name + ": " + age);
    }
}
