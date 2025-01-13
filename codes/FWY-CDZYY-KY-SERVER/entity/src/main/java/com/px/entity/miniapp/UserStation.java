package com.px.entity.miniapp;

import com.baomidou.mybatisplus.annotation.TableName;
import com.px.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户所属站点表
 * </p>
 *
 * @author 品讯科技
 * @since 2024-08-19
 */
@Getter
@Setter
@TableName("t_user_station")
@Accessors(chain = true)
public class UserStation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long userId;

    /**
     * 站点id
     */
    private String stationId;

    /**
     * 手机号
     */
    private String mobilePhone;

    /**
     * 用户在该站点的充电量
     */
    private Double totalPower;
}
