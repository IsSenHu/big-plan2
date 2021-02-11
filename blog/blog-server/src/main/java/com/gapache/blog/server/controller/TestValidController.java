package com.gapache.blog.server.controller;

import com.gapache.commons.model.JsonResult;
import com.gapache.web.Check;
import com.gapache.web.Validating;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

/**
 * @author HuSen
 * @since 2020/11/20 9:09 上午
 */
@RestController
@Validating
public class TestValidController {

    @Data
    private static class ValidDTO {
        @NotBlank(message = "哈哈哈哈哈哈")
        private String name;
    }

    @GetMapping("/testValid")
    public JsonResult<String> testValid(@Check ValidDTO dto) {
        return JsonResult.of(String.valueOf(dto.name));
    }
}
