package com.px.entity.admin;

import com.baomidou.mybatisplus.annotation.TableName;
import com.px.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author 品讯科技
 * @since 2023-09-23
 */
@Getter
@Setter
@TableName("t_role")
public class Role extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公司id
     */
    private Long companyId;

    /**
     * 父角色id
     */
    private Long parentId;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String roleDesc;

    /**
     * 角色列表
     */
    private String permissions;
}
