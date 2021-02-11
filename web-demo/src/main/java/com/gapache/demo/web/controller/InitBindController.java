package com.gapache.demo.web.controller;

import com.gapache.demo.web.model.Cat;
import com.gapache.demo.web.model.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HuSen
 * @since 2021/1/6 11:08 上午
 */
@RestController
public class InitBindController {

    @GetMapping("/initBind")
    public Map<String, String> initBind(@ModelAttribute("person") Person person, @ModelAttribute("cat") Cat cat) {
        Map<String, String> map = new HashMap<>(2);
        map.put("person.name", person.getName());
        map.put("cat.name", cat.getName());
        return map;
    }
}
