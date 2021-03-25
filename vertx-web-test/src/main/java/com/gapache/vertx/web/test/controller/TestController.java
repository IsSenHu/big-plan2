package com.gapache.vertx.web.test.controller;

import com.gapache.commons.model.JsonResult;
import com.gapache.vertx.core.VertxManager;
import com.gapache.vertx.web.annotation.GetRouting;
import com.gapache.vertx.web.annotation.PostRouting;
import com.gapache.vertx.web.annotation.RequestRouting;
import com.gapache.vertx.web.annotation.VertxController;
import com.gapache.vertx.web.test.client.Test2Client;
import com.gapache.vertx.web.test.client.Test4Client;
import com.gapache.vertx.web.test.pojo.TestPoJo;
import io.vertx.core.shareddata.SharedData;

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
    private Test4Client test4Client;

    @GetRouting
    public JsonResult<String> test() {
        test2Client.test2("胡森", 25).onComplete(event -> {
            if (event.succeeded()) {
                System.out.println(event.result());
            } else {
                event.cause().printStackTrace();
            }
        });

        TestPoJo testPoJo = new TestPoJo();
        testPoJo.setServiceName("test");

        test4Client.dynamic(testPoJo).onComplete(event -> {
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

    @GetRouting("/test3")
    public JsonResult<String> test3() {
        VertxManager.getVertx().eventBus().publish("vertx.health.check.address", System.currentTimeMillis() + "");
        SharedData sharedData = VertxManager.getVertx().sharedData();
        sharedData.<String, String>getAsyncMap("myMap", res -> {
            if (res.succeeded()) {
                res.result().put("name", "胡森", putRes -> {
                    if (putRes.succeeded()) {
                        System.out.println("数据放入成功");
                    }
                });
            }
        });
        return JsonResult.success();
    }

    @GetRouting("/test4")
    public JsonResult<String> test4() {
        SharedData sharedData = VertxManager.getVertx().sharedData();
        sharedData.<String, String>getAsyncMap("myMap", res -> {
            if (res.succeeded()) {
                res.result().get("name", getRes -> {
                    if (getRes.succeeded()) {
                        System.out.println("获取到名称: " + getRes.result());
                    }
                });
            }
        });
        return JsonResult.success();
    }
}
