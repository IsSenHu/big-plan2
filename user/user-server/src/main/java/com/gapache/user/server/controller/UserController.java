package com.gapache.user.server.controller;

import com.gapache.commons.model.JsonResult;
import com.gapache.security.annotation.AuthResource;
import com.gapache.security.annotation.NeedAuth;
import com.gapache.user.common.model.vo.UserVO;
import com.gapache.user.server.service.UserService;
import com.gapache.web.Check;
import com.gapache.web.Validating;
import org.springframework.web.bind.annotation.*;

/**
 * @author HuSen
 * @since 2021/1/25 1:12 下午
 */
@Validating
@RestController
@RequestMapping("/api/user")
@NeedAuth("User")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @AuthResource(scope = "create", name = "创建用户")
    public JsonResult<UserVO> create(@RequestBody @Check UserVO vo) {
        UserVO userVO = userService.create(vo);
        return JsonResult.of(userVO);
    }

    @PutMapping
    @AuthResource(scope = "update", name = "更新用户")
    public JsonResult<UserVO> update(@RequestBody @Check UserVO vo) {
        return null;
    }

    @DeleteMapping("/{id}")
    @AuthResource(scope = "delete", name = "删除用户")
    public JsonResult<Boolean> delete(@PathVariable Long id) {
        return null;
    }

    @GetMapping("/{id}")
    @AuthResource(scope = "get", name = "根据ID查询用户")
    public JsonResult<UserVO> get(@PathVariable Long id) {
        return null;
    }

    @GetMapping("/findByUsername/{username}")
    @AuthResource(scope = "findByUsername", name = "根据用户名查询用户")
    public JsonResult<UserVO> findByUsername(@PathVariable String username, @RequestParam(required = false) String clientId) {
        UserVO vo = userService.findByUsername(username, clientId);
        return JsonResult.of(vo);
    }

    @GetMapping("/userIsExisted/{id}")
    @AuthResource(scope = "userIsExisted", name = "判断用户是否存在")
    public JsonResult<Boolean> userIsExisted(@PathVariable Long id) {
        Boolean isExisted = userService.userIsExisted(id);
        return JsonResult.of(isExisted);
    }
}
