package com.gapache.demo.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HuSen
 * @since 2021/1/6 11:25 上午
 */
@RestController
public class ExceptionController {

    @GetMapping("/exception")
    public void exception(Integer id) {
        if (id < 0) {
            throw new RuntimeException("id is lower zero!!");
        }
    }
}
