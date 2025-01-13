package com.px.entity.miniapp;

import com.baomidou.mybatisplus.annotation.TableName;
import com.px.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * APP日志表
 * </p>
 *
 * @author 品讯科技
 * @since 2023-08-20
 */
@Getter
@Setter
@TableName("t_app_log")
public class AppLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公司id
     */
    private Long companyId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 操作人
     */
    private String username;

    /**
     * ip
     */
    private String ip;

    /**
     * 操作名
     */
    private String operation;

    /**
     * 方法
     */
    private String method;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 执行时长
     */
    private Long executeTime;
}
