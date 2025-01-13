package com.px.entity.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 品讯科技
 * @description 用户
 * @date 2024-08
 */
@Data
public class CustomUserVo {
    private Long userId;
    private String userName;
    private String mobilePhone;
    private Integer status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime registerTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;
    private int rechargeTimes;
    private int rechargeAmount;
    private int chargeTimes;
    private double totalPower;
    private int totalMoney;
    private int payAmount;
    private int discountAmount;
    private int balance;
    private int frozenAmount;
    private Long refundTimes;
    private int refundAmount;
    /**
     * 退款扣除优惠总金额
     */
    private int refundDiscountAmount;

}
