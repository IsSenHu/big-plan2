package com.gapache.user.sdk.feign;

import com.gapache.commons.model.JsonResult;
import com.gapache.user.common.model.vo.UserVO;
import com.gapache.user.sdk.fallback.UserServerFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author HuSen
 * @since 2020/8/26 9:33 上午
 */
@FeignClient(value = "user-server", path = "/api/user", fallback = UserServerFeignFallback.class)
public interface UserServerFeign {

    @PostMapping
    JsonResult<UserVO> create(@RequestBody UserVO vo);

    @GetMapping("/userIsExisted/{id}")
    JsonResult<Boolean> userIsExisted(@PathVariable Long id);

    @GetMapping("/findByUsername/{username}")
    JsonResult<UserVO> findByUsername(@PathVariable String username, @RequestParam(required = false) String clientId);
}
