package com.px.service.admin.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.px.entity.admin.RolePermission;
import com.px.mapper.admin.RolePermissionMapper;
import com.px.service.admin.RolePermissionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色权限表 服务实现类
 * </p>
 *
 * @author 品讯科技
 * @since 2023-07-12
 */
@Service
@DS("db-admin")
public class RolePermissionServiceImpl extends MPJBaseServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

}
