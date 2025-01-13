package com.px.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.px.common.utils.CommUtil;
import com.px.entity.admin.Role;
import com.px.mapper.admin.RoleMapper;
import com.px.service.admin.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author 品讯科技
 * @since 2023-09-23
 */
@Service
public class RoleServiceImpl extends MPJBaseServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public Object listRole(String roleName) {
        return lambdaQuery().eq(!CommUtil.isEmptyOrNull(roleName), Role::getRoleName, roleName).list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Object add(Role role) {
        role.setId(null);
        return save(role);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void modify(Role role) {
        updateById(role);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void remove(int id) {
        removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveList(List<Role> roleList) {

        roleList.forEach(role -> {
            UpdateWrapper update = new UpdateWrapper();
            update.eq("id", role.getId());
            update.set("permissions", role.getPermissions());
            baseMapper.update(role, update);
        });
    }

    @Override
    public Role detail(int id) {
        return baseMapper.selectById(id);
    }
}
