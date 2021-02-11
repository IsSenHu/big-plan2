package com.gapache.cloud.money.management.server.service;

import com.gapache.cloud.money.management.common.model.FundDTO;
import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;

import java.util.List;

/**
 * @author HuSen
 * @since 2021/1/13 2:40 下午
 */
public interface FundService {

    Boolean add(List<FundDTO> dtoList);

    PageResult<FundDTO> page(IPageRequest<FundDTO> pageRequest);

    Boolean loadData(String path);

    List<FundDTO> queryHold();

    Boolean joinOptional(String code);

    List<FundDTO> queryOptional();

    Boolean joinHold(String code);
}
