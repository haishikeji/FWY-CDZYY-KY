package com.px.entity.enplus;

import lombok.Data;

/**
 * @author 品讯科技
 * @description 推送停止充电结果
 * @date 2024-08
 */
@Data
public class EnNotificationStartChargeResult {

    /**
     * 充电订单号
     */
    private String StartChargeSeq;
    /**
     * 充电订单状态
     * 1：启动中
     * 2：充电中
     * 3：停止中
     * 4：已结束
     * 5：未知
     */
    private int StartChargeSeqStat;

    /**
     * 充电设备接口编码
     */
    private String ConnectorID;

    /**
     * 充电启动时间
     */
    private String StartTime;

    /**
     * 启动充电时收到的验证码，用于充电桩手动停止充电
     */
    private String IdentCode;
}
