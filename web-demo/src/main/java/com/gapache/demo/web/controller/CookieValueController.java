package com.gapache.demo.web.controller;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author HuSen
 * @since 2021/1/6 11:44 上午
 */
@RestController
public class CookieValueController {

    @GetMapping("/cookieValue")
    public Map<String, Object> cookieValue(@CookieValue("JSESSIONID") String cookie) {
        Map<String, Object> map = new HashMap<>(1);
        map.put("cookie", cookie);
        return map;
    }
}
