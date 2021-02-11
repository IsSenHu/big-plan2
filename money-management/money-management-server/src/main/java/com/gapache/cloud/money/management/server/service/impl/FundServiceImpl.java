package com.gapache.cloud.money.management.server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.gapache.cloud.money.management.common.model.FundDTO;
import com.gapache.cloud.money.management.common.model.FundNetWorthDTO;
import com.gapache.cloud.money.management.common.model.MoneyManagementError;
import com.gapache.cloud.money.management.server.dao.entity.FundEntity;
import com.gapache.cloud.money.management.server.dao.repository.FundRepository;
import com.gapache.cloud.money.management.server.service.FundNetWorthService;
import com.gapache.cloud.money.management.server.service.FundService;
import com.gapache.cloud.money.management.server.transform.FundTransform;
import com.gapache.commons.model.IPageRequest;
import com.gapache.commons.model.PageResult;
import com.gapache.commons.model.ThrowUtils;
import com.gapache.commons.utils.IStringUtils;
import com.gapache.commons.utils.TimeUtils;
import com.gapache.jpa.FindUtils;
import com.gapache.jpa.PageHelper;
import com.gapache.jpa.SpecificationFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author HuSen
 * @since 2021/1/13 2:40 下午
 */
@Slf4j
@Service
public class FundServiceImpl implements FundService {

    private final FundRepository fundRepository;

    private final FundNetWorthService fundNetWorthService;

    private final TaskExecutor taskExecutor;

    public FundServiceImpl(FundRepository fundRepository, FundNetWorthService fundNetWorthService, TaskExecutor taskExecutor) {
        this.fundRepository = fundRepository;
        this.fundNetWorthService = fundNetWorthService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(List<FundDTO> dtoList) {
        List<FundEntity> fundEntities = fundRepository.findAllByCodeIn(dtoList.stream().map(FundDTO::getCode).collect(Collectors.toSet()));
        Map<String, FundEntity> codeEntityMap = fundEntities.stream().collect(Collectors.toMap(FundEntity::getCode, e -> e));

        dtoList.forEach(dto -> {
            if (codeEntityMap.containsKey(dto.getCode())) {
                // 更新
                FundEntity old = codeEntityMap.get(dto.getCode());
                old.setName(dto.getName());
            } else {
                // 新增
                FundEntity entity = new FundEntity();
                BeanUtils.copyProperties(dto, entity);
                codeEntityMap.put(dto.getCode(), entity);
            }
        });

        if (codeEntityMap.size() == 0) {
            return true;
        }

        return fundRepository.saveAll(codeEntityMap.values()).size() > 0;
    }

    @Override
    public PageResult<FundDTO> page(IPageRequest<FundDTO> pageRequest) {
        Pageable pageable = PageHelper.of(pageRequest);

        FundDTO params = pageRequest.getCustomParams();
        // 支持动态多条件查询
        Specification<FundEntity> specification = SpecificationFactory.produce((predicates, root, criteriaBuilder) -> {
            if (params != null) {
                String code = params.getCode();
                if (StringUtils.isNotBlank(code)) {
                    predicates.add(criteriaBuilder.equal(root.get("code").as(String.class), code));
                }

                String name = params.getName();
                if (StringUtils.isNotBlank(name)) {
                    predicates.add(criteriaBuilder.like(root.get("name").as(String.class), FindUtils.allMatch(name)));
                }

                Boolean optional = params.getOptional();
                if (optional != null) {
                    predicates.add(criteriaBuilder.equal(root.get("optional").as(Boolean.class), optional));
                }

                Boolean hold = params.getHold();
                if (hold != null) {
                    predicates.add(criteriaBuilder.equal(root.get("hold").as(Boolean.class), hold));
                }
            }
        });
        Page<FundEntity> page = fundRepository.findAll(specification, pageable);
        return PageResult.of(page.getTotalElements(), new FundTransform()::toT, page.getContent());
    }

    @Override
    public Boolean joinHold(String code) {
        FundEntity entity = fundRepository.findByCode(code);
        ThrowUtils.throwIfTrue(entity == null, MoneyManagementError.FUND_NOT_FOUND);

        entity.setHold(entity.getHold() == null || !entity.getHold());
        fundRepository.save(entity);
        return true;
    }

    @Override
    public List<FundDTO> queryHold() {
        List<FundEntity> entities = fundRepository.findAllByHold(true);
        return entities.stream().map(new FundTransform()::toT).collect(Collectors.toList());
    }

    @Override
    public Boolean joinOptional(String code) {
        FundEntity entity = fundRepository.findByCode(code);
        ThrowUtils.throwIfTrue(entity == null, MoneyManagementError.FUND_NOT_FOUND);

        entity.setOptional(entity.getOptional() == null || !entity.getOptional());
        fundRepository.save(entity);
        return true;
    }

    @Override
    public List<FundDTO> queryOptional() {
        List<FundEntity> entities = fundRepository.findAllByOptional(true);
        return entities.stream().map(new FundTransform()::toT).collect(Collectors.toList());
    }

    @Override
    public Boolean loadData(String path) {
        taskExecutor.execute(() -> {
            try {
                File dir = new File(path);
                List<FundDTO> dtoList = new ArrayList<>();
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    assert files != null;
                    for (File file : files) {
                        String code = "";
                        String name = "";
                        log.info("start parse {}", file.getName());
                        List<FundNetWorthDTO> fundNetWorthDTOList = new ArrayList<>();
                        byte[] bytes = FileCopyUtils.copyToByteArray(file);
                        String data = IStringUtils.newString(bytes);
                        JSONArray objects = JSONArray.parseArray(data);
                        int size = objects.size();
                        for (int i = 0; i < size; i++) {
                            List<String> fund;
                            try {
                                fund = objects.getJSONArray(i).toJavaList(String.class);
                            } catch (Exception e) {
                                continue;
                            }

                            String day = fund.get(0);
                            String netWorth = fund.get(1);
                            String addUpNetWorth = fund.get(2);
                            code = fund.get(3);
                            name = fund.get(4);
                            FundNetWorthDTO fundNetWorthDTO = new FundNetWorthDTO();
                            fundNetWorthDTO.setCode(fund.get(3));
                            fundNetWorthDTO.setDay(TimeUtils.parseLocalDate(TimeUtils.Format._1, day));
                            if (StringUtils.isNotBlank(netWorth)) {
                                fundNetWorthDTO.setNetWorth(Double.parseDouble(netWorth));
                            }
                            if (StringUtils.isNotBlank(addUpNetWorth)) {
                                fundNetWorthDTO.setAddUpNetWorth(Double.parseDouble(addUpNetWorth));
                            }
                            fundNetWorthDTOList.add(fundNetWorthDTO);
                        }
                        fundNetWorthService.add(fundNetWorthDTOList);
                        if (StringUtils.isNotBlank(code) && StringUtils.isNotBlank(name)) {
                            FundDTO dto = new FundDTO();
                            dto.setCode(code);
                            dto.setName(name);
                            dtoList.add(dto);
                        }
                        log.info("end parse {}", file.getName());
                    }
                }
                add(dtoList);
                log.info("end all");
            } catch (Exception e) {
                log.error("loadData error:{}", path, e);
            }
        });
        return true;
    }
}
