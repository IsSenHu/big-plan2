package com.gapache.demo.web.advice;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 * 全局数据绑定
 * 全局数据预处理
 *
 * @author HuSen
 * @since 2021/1/6 11:12 上午
 */
@ControllerAdvice
public class WebAdvice {

    /**
     * 全局异常处理
     *
     * @param e 异常
     * @return ModelAndView
     */
    @ExceptionHandler
    public ModelAndView customException(Exception e) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("message", e.getMessage());
        mv.setViewName("my_error");
        return mv;
    }

    /**
     * 全局数据绑定
     * TODO 装载用户凭证信息
     *
     * @return 绑定的数据
     */
    @ModelAttribute("mine")
    public Map<String, Object> myData() {
        Map<String, Object> map = new HashMap<>(2);
        map.put("age", 99);
        map.put("gender", "男");
        return map;
    }

    /**
     * 全局数据预处理
     *
     * @param binder WebDataBinder
     */
    @InitBinder
    public void person(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("person.");
    }

    @InitBinder
    public void cat(WebDataBinder binder) {
        binder.setFieldDefaultPrefix("cat.");
    }
}
