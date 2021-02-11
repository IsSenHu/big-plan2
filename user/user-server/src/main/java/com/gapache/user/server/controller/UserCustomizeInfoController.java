package com.gapache.user.server.controller;

import com.gapache.commons.model.JsonResult;
import com.gapache.security.annotation.AuthResource;
import com.gapache.security.annotation.NeedAuth;
import com.gapache.user.common.model.vo.UserCustomizeInfoVO;
import com.gapache.web.Check;
import com.gapache.web.Validating;
import org.springframework.web.bind.annotation.*;

/**
 * @author HuSen
 * @since 2021/1/25 1:12 下午
 */
@Validating
@RestController
@RequestMapping("/api/userCustomizeInfo")
@NeedAuth("UserCustomizeInfo")
public class UserCustomizeInfoController {

    @PostMapping
    @AuthResource(scope = "create", name = "创建用户自定义信息")
    public JsonResult<UserCustomizeInfoVO> create(@RequestBody @Check UserCustomizeInfoVO vo) {
        return null;
    }

    @PutMapping
    @AuthResource(scope = "update", name = "更新用户自定义信息")
    public JsonResult<UserCustomizeInfoVO> update(@RequestBody @Check UserCustomizeInfoVO vo) {
        return null;
    }

    @DeleteMapping("/{id}")
    @AuthResource(scope = "delete", name = "删除用户自定义信息")
    public JsonResult<Boolean> delete(@PathVariable Long id) {
        return null;
    }

    @GetMapping("/{id}")
    @AuthResource(scope = "get", name = "根据ID查询用户自定义信息")
    public JsonResult<UserCustomizeInfoVO> get(@PathVariable Long id) {
        return null;
    }
}
