package com.gapache.vertx.web.test.client;

import com.gapache.commons.model.JsonResult;
import com.gapache.vertx.web.annotation.PostRouting;
import com.gapache.vertx.web.annotation.ZeusClient;
import io.vertx.core.Future;

/**
 * @author HuSen
 * @since 2021/3/2 6:35 下午
 */
@ZeusClient(value = "test", path = "/test")
public interface Test2Client {

    @PostRouting("/test2")
    Future<JsonResult<String>> test2(String name, Integer age);
}
