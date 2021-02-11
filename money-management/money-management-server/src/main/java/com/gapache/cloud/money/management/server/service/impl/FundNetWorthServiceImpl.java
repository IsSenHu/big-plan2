package com.gapache.cloud.money.management.server.service.impl;

import com.gapache.cloud.money.management.common.model.*;
import com.gapache.cloud.money.management.server.dao.entity.FundEntity;
import com.gapache.cloud.money.management.server.dao.entity.FundNetWorthEntity;
import com.gapache.cloud.money.management.server.dao.repository.FundNetWorthRepository;
import com.gapache.cloud.money.management.server.dao.repository.FundRepository;
import com.gapache.cloud.money.management.server.service.FundNetWorthService;
import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;
import com.gapache.commons.model.ThrowUtils;
import com.gapache.commons.utils.TimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HuSen
 * @since 2021/1/14 10:06 上午
 */
@Service
public class FundNetWorthServiceImpl implements FundNetWorthService {

    private final FundNetWorthRepository fundNetWorthRepository;

    private final FundRepository fundRepository;

    public FundNetWorthServiceImpl(FundNetWorthRepository fundNetWorthRepository, FundRepository fundRepository) {
        this.fundNetWorthRepository = fundNetWorthRepository;
        this.fundRepository = fundRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(List<FundNetWorthDTO> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return true;
        }
        Map<LocalDate, FundNetWorthDTO> dayMap = dtoList.stream().collect(Collectors.toMap(FundNetWorthDTO::getDay, f -> f));
        String code = dtoList.get(0).getCode();

        List<FundNetWorthEntity> entities = fundNetWorthRepository.findAllByCodeAndDayIn(code, dayMap.keySet());
        Map<LocalDate, FundNetWorthEntity> dayEntityMap = entities.stream().collect(Collectors.toMap(FundNetWorthEntity::getDay, e -> e));

        List<FundNetWorthEntity> newEntities = new ArrayList<>();
        for (FundNetWorthDTO dto : dtoList) {
            if (dayEntityMap.containsKey(dto.getDay())) {
                continue;
            }
            FundNetWorthEntity entity = new FundNetWorthEntity();
            BeanUtils.copyProperties(dto, entity);
            newEntities.add(entity);
        }

        if (newEntities.size() == 0) {
            return true;
        }

        return fundNetWorthRepository.saveAll(newEntities).size() > 0;
    }

    @Override
    public PageResult<FundNetWorthDTO> page(IPageRequest<FundNetWorthDTO> pageRequest) {
        Pageable pageable = PageRequest.of(pageRequest.getPage() - 1, pageRequest.getNumber());
        FundNetWorthDTO params = pageRequest.getCustomParams();
        Page<FundNetWorthEntity> page;
        if (StringUtils.isNotBlank(params.getCode())) {
            page = fundNetWorthRepository.findAllByCode(params.getCode(), pageable);
        } else {
            page = fundNetWorthRepository.findAll(pageable);
        }

        return PageResult.of(page.getTotalElements(), po -> {
            FundNetWorthDTO dto = new FundNetWorthDTO();
            BeanUtils.copyProperties(po, dto);
            dto.setId(po.getId());
            return dto;
        }, page.getContent());
    }

    @Override
    public LineDataDTO<String, Double> queryLineData(QueryFundNetWorthLineDataDTO dto) {
        LineDataDTO<String, Double> lineData = new LineDataDTO<>();
        lineData.setXList(new ArrayList<>());
        lineData.setYList(new ArrayList<>());
        lineData.setOtherInfo(new HashMap<>(2));

        FundEntity entity = fundRepository.findByCode(dto.getCode());
        ThrowUtils.throwIfTrue(entity == null, MoneyManagementError.FUND_NOT_FOUND);

        LocalDate start = dto.getStart();
        LocalDate end = dto.getEnd();
        List<FundNetWorthEntity> entities;
        if (start == null && end == null) {
            entities = fundNetWorthRepository.findAllByCode(dto.getCode());
        } else if (start != null && end == null) {
            entities = fundNetWorthRepository.findAllByCodeAndDayGreaterThanEqual(dto.getCode(), start);
        } else if (start == null && end != null) {
            entities = fundNetWorthRepository.findAllByCodeAndDayLessThanEqual(dto.getCode(), end);
        } else {
            entities = fundNetWorthRepository.findAllByCodeAndDayGreaterThanEqualAndDayLessThanEqual(dto.getCode(), start, end);
        }

        if (CollectionUtils.isEmpty(entities)) {
            return lineData;
        }

        // 从前往后排序
        entities.sort(Comparator.comparingLong(e -> e.getDay().toEpochDay()));

        // 取出第一个作为计算基准
        FundNetWorthEntity first = entities.get(0);
        boolean base = NetWorthType.valueOf(dto.getNetWorthType()).equals(NetWorthType.BASE);
        Double firstNetWorth = base ? first.getNetWorth() : first.getAddUpNetWorth();
        for (FundNetWorthEntity worthEntity : entities) {
            lineData.getXList().add(TimeUtils.format(TimeUtils.Format._1, worthEntity.getDay()));
            if (dto.getIsNumber()) {
                lineData.getYList().add(base ? worthEntity.getNetWorth() : worthEntity.getAddUpNetWorth());
            } else {
                // 相对于基准的增长
                lineData.getYList().add((((base ? worthEntity.getNetWorth() : worthEntity.getAddUpNetWorth()) - firstNetWorth) / firstNetWorth) * 100);
            }
        }

        lineData.getOtherInfo().put("code", entity.getCode());
        lineData.getOtherInfo().put("name", entity.getName());

        return lineData;
    }
}
