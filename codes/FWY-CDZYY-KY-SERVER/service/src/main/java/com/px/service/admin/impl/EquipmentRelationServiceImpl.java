package com.px.service.admin.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.px.entity.admin.EquipmentRelation;
import com.px.mapper.admin.EquipmentRelationMapper;
import com.px.service.admin.EquipmentRelationService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 品讯科技
 * @since 2023-08-07
 */
@Service
@DS("db-admin")
public class EquipmentRelationServiceImpl extends MPJBaseServiceImpl<EquipmentRelationMapper, EquipmentRelation> implements EquipmentRelationService {

    @Override
    public EquipmentRelation getByShortId(String shortId) {
        return lambdaQuery().eq(EquipmentRelation::getShortId, shortId).one();
    }

    @Override
    public EquipmentRelation getByEquipmentId(String equipmentId) {
        return lambdaQuery().eq(EquipmentRelation::getEquipmentId, equipmentId).one();
    }
}
