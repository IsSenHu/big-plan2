package com.gapache.demo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author HuSen
 * @since 2021/1/6 11:27 上午
 */
@Controller
public class MineController {

    @GetMapping("/mine")
    public String mine() {
        return "mine";
    }
}
