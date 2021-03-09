package com.gapache.vertx.web.test.client;

import com.gapache.commons.model.JsonResult;
import com.gapache.vertx.web.annotation.GetRouting;
import com.gapache.vertx.web.annotation.PostRouting;
import com.gapache.vertx.web.annotation.ZeusClient;
import com.gapache.vertx.web.test.pojo.TestPoJo;
import io.vertx.core.Future;

/**
 * @author HuSen
 * @since 2021/3/3 12:37 下午
 */
@ZeusClient(value = "test2", path = "/test3")
public interface Test4Client {

    @GetRouting("/hello")
    Future<JsonResult<String>> hello(String name);

    @PostRouting("/dynamic")
    Future<JsonResult<TestPoJo>> dynamic(TestPoJo poJo);
}
