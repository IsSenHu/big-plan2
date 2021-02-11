package com.gapache.cloud.money.management.server.controller;

import com.gapache.cloud.money.management.common.model.FundNetWorthDTO;
import com.gapache.cloud.money.management.common.model.LineDataDTO;
import com.gapache.cloud.money.management.common.model.QueryFundNetWorthLineDataDTO;
import com.gapache.cloud.money.management.server.service.FundNetWorthService;
import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.JsonResult;
import com.gapache.commons.model.PageResult;
import com.gapache.security.annotation.AuthResource;
import com.gapache.security.annotation.NeedAuth;
import com.gapache.web.Check;
import com.gapache.web.Validating;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HuSen
 * @since 2021/1/14 10:51 上午
 */
@Validating
@RestController
@NeedAuth("FundNetWorth")
@RequestMapping("/api/fundNetWorth")
public class FundNetWorthController {

    private final FundNetWorthService fundNetWorthService;

    public FundNetWorthController(FundNetWorthService fundNetWorthService) {
        this.fundNetWorthService = fundNetWorthService;
    }

    @PostMapping("/page")
    @AuthResource(scope = "page", name = "分页查询基金净值")
    public JsonResult<PageResult<FundNetWorthDTO>> page(@RequestBody IPageRequest<FundNetWorthDTO> pageRequest) {
        return JsonResult.of(fundNetWorthService.page(pageRequest));
    }

    @PostMapping("/queryLineData")
    public JsonResult<LineDataDTO<String, Double>> queryLineData(@RequestBody @Check QueryFundNetWorthLineDataDTO dto) {
        return JsonResult.of(fundNetWorthService.queryLineData(dto));
    }
}
