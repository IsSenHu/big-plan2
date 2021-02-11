package com.gapache.cloud.money.management.server.controller;

import com.gapache.cloud.money.management.common.model.FundDTO;
import com.gapache.cloud.money.management.server.service.FundService;
import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.JsonResult;
import com.gapache.commons.model.PageResult;
import com.gapache.security.annotation.AuthResource;
import com.gapache.security.annotation.NeedAuth;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author HuSen
 * @since 2021/1/13 2:49 下午
 */
@RestController
@RequestMapping("/api/fund")
@NeedAuth("Fund")
public class FundController {

    private final FundService fundService;

    public FundController(FundService fundService) {
        this.fundService = fundService;
    }

    @PostMapping
    @AuthResource(scope = "add", name = "添加基金")
    public JsonResult<Boolean> add(@RequestBody List<FundDTO> dtoList) {
        return JsonResult.of(fundService.add(dtoList));
    }

    @PostMapping("/page")
    @AuthResource(scope = "page", name = "分页查询基金")
    public JsonResult<PageResult<FundDTO>> page(@RequestBody IPageRequest<FundDTO> pageRequest) {
        return JsonResult.of(fundService.page(pageRequest));
    }

    @GetMapping("/loadData")
    @AuthResource(scope = "loadData", name = "加载基金数据")
    public JsonResult<Boolean> loadData(String path) {
        return JsonResult.of(fundService.loadData(path));
    }

    @PostMapping("/joinHold")
    @AuthResource(scope = "joinHold", name = "基金加入持有")
    public JsonResult<Boolean> joinHold(String code) {
        return JsonResult.of(fundService.joinHold(code));
    }

    @GetMapping("/queryHold")
    @AuthResource(scope = "queryHold", name = "基金查询持有")
    public JsonResult<List<FundDTO>> queryHold() {
        return JsonResult.of(fundService.queryHold());
    }

    @PostMapping("/joinOptional")
    @AuthResource(scope = "joinOptional", name = "加入自选")
    public JsonResult<Boolean> joinOptional(String code) {
        return JsonResult.of(fundService.joinOptional(code));
    }

    @GetMapping("/queryOptional")
    @AuthResource(scope = "queryOptional", name = "查询自选")
    public JsonResult<List<FundDTO>> queryOptional() {
        return JsonResult.of(fundService.queryOptional());
    }
}
