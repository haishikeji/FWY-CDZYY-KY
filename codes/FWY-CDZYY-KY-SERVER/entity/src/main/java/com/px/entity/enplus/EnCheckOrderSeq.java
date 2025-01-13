package com.px.entity.enplus;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 品讯科技
 * @description 推送订单对账信息
 * @date 2024-08
 */
@Data
public class EnCheckOrderSeq {

    private String CheckOrderSeq;
    private String StartTime;
    private String EndTime;
    /**
     * 订单数量N
     */
    private int OrderCount;
    /**
     * 总电量
     */
    private BigDecimal TotalOrderPower;
    /**
     * 总金额
     */
    private BigDecimal TotalOrderMoney;
    private List<EnChargeOrder> ChargeOrders;

}
