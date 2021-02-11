package com.gapache.cloud.money.management.server.transform;

import com.gapache.cloud.money.management.common.model.FundDTO;
import com.gapache.cloud.money.management.server.dao.entity.FundEntity;
import com.gapache.commons.transform.Transform;
import org.springframework.beans.BeanUtils;

/**
 * @author HuSen
 * @since 2021/1/15 11:48 上午
 */
public class FundTransform implements Transform<FundDTO, FundEntity> {

    @Override
    public FundDTO toT(FundEntity fundEntity) {
        FundDTO dto = new FundDTO();
        BeanUtils.copyProperties(fundEntity, dto);
        dto.setId(fundEntity.getId());
        return dto;
    }

    @Override
    public FundEntity toR(FundDTO dto) {
        FundEntity entity = new FundEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(dto.getId());
        return entity;
    }
}
