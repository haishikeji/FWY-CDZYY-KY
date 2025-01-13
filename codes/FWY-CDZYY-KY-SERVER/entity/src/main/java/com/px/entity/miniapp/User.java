package com.px.entity.miniapp;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.px.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author 品讯科技
 * @since 2023-07-26
 */
@Getter
@Setter
@TableName(value = "t_user")
public class User extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 微信unionid
     */
    private String unionid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码:返回前端不进行序列化
     */
    @JsonIgnore
    private String password;

    /**
     * 性别：0-女 1-男
     */
    private Integer gender;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String mobilePhone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 0:禁用 1:启用
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;
}
