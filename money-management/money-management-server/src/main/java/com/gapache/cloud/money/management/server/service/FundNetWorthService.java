package com.gapache.cloud.money.management.server.service;

import com.gapache.cloud.money.management.common.model.FundNetWorthDTO;
import com.gapache.cloud.money.management.common.model.LineDataDTO;
import com.gapache.cloud.money.management.common.model.QueryFundNetWorthLineDataDTO;
import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;

import java.util.List;

/**
 * @author HuSen
 * @since 2021/1/14 10:06 上午
 */
public interface FundNetWorthService {

    Boolean add(List<FundNetWorthDTO> dtoList);

    PageResult<FundNetWorthDTO> page(IPageRequest<FundNetWorthDTO> pageRequest);

    LineDataDTO<String, Double> queryLineData(QueryFundNetWorthLineDataDTO dto);
}
