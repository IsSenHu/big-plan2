package com.gapache.health.server.controller;

import com.gapache.commons.model.JsonResult;
import com.gapache.health.server.model.sleep.SleepCreateVO;
import com.gapache.health.server.service.SleepService;
import com.gapache.user.sdk.feign.UserServerFeign;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author HuSen
 * create on 2020/5/9 9:25 上午
 */
@RestController
@RequestMapping("/api/sleep")
public class SleepController {

    private final SleepService sleepService;

    @Resource
    private UserServerFeign userServerFeign;

    public SleepController(SleepService sleepService) {
        this.sleepService = sleepService;
    }

    @PutMapping
    public JsonResult<Long> create(@RequestBody SleepCreateVO vo) {
        return sleepService.create(vo);
    }

    @GetMapping
    public JsonResult<String> test(int a) {
        return userServerFeign.degrade2(a);
    }
}
