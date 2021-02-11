package com.gapache.demo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author HuSen
 * @since 2021/1/6 10:40 上午
 */
@Controller
public class ModelAttributeController {

    @ModelAttribute("abc")
    public String populateModel(@RequestParam String abc) {
        return abc;
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
