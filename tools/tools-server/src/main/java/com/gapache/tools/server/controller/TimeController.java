package com.gapache.tools.server.controller;

import com.gapache.commons.model.JsonResult;
import com.gapache.commons.utils.TimeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * @author HuSen
 * @since 2021/2/2 9:43 上午
 */
@RestController
@RequestMapping("/api/time")
public class TimeController {

    @GetMapping("/howLong/{start}")
    public JsonResult<Long> howLong(@PathVariable String start) {
        LocalDate now = LocalDate.now();
        LocalDate localDate = TimeUtils.parseLocalDate(TimeUtils.Format._1, start);
        return JsonResult.of(now.toEpochDay() - localDate.toEpochDay());
    }
}
