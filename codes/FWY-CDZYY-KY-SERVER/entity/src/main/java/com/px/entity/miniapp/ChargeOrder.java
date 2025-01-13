package com.px.entity.miniapp;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.px.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 充电订单表
 * </p>
 *
 * @author 品讯科技
 * @since 2023-08-08
 */
@Getter
@Setter
@TableName("t_charge_order")
@Accessors(chain = true)
public class ChargeOrder extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public static int ORDER_STATUS_未知 = 0;
    public static int ORDER_STATUS_成功 = 1;
    public static int ORDER_STATUS_失败 = 2;
    public static int ORDER_STATUS_取消 = 3;


    public static int CHARGE_STATUS_预约中 = 0;
    public static int CHARGE_STATUS_启动中 = 1;
    public static int CHARGE_STATUS_充电中 = 2;
    public static int CHARGE_STATUS_停止中 = 3;
    public static int CHARGE_STATUS_已结束 = 4;
    public static int CHARGE_STATUS_已取消 = 5;


    public static int INVOICE_STATUS_待开票 = 0;
    public static int INVOICE_STATUS_已开票 = 1;
    public static int INVOICE_STATUS_已作废 = 2;
    public static int INVOICE_STATUS_开票中 = 3;


    public static int IS_BOOKING_否 = 0;
    public static int IS_BOOKING_是 = 1;

    // 充电停止原因：0：用户手动停止，1：运营平台停止，2：BMS停止，3：充电机器设备故障，4：连接器断开
    public static int STOP_REASON_用户手动停止 = 0;
    public static int STOP_REASON_运营平台停止 = 1;
    public static int STOP_REASON_BMS停止 = 2;
    public static int STOP_REASON_充电机器设备故障 = 3;
    public static int STOP_REASON_连接器断开 = 4;
    public static int STOP_REASON_预约启动充电失败 = 5;

    private Long userId;

    private String stationId;

    /**
     * 充电订单号（EN+）
     */
    private String startChargeSeq;

    /**
     * 充电设备接口编码（EN+）
     */
    private String connectorId;

    /**
     * 是否为预约订单：0-否 1-是
     */
    private Integer isBooking;

    /**
     * 充电开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 充电结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 电池剩余电量
     */
    private Double soc;

    /**
     * 累积充电量（度）
     */
    private Double totalPower;

    /**
     * 累积总金额（元）
     */
    private Integer totalMoney;

    /**
     * 累积电费（元）
     */
    private Integer elecMoney;

    /**
     * 累积服务费（元）
     */
    private Integer serviceMoney;

    /**
     * 实付金额（分）
     */
    private Integer payAmount;

    /**
     * 优惠方式：RechargeRights-充值权益 Coupon-优惠券
     */
    private String discountType;

    /**
     * 优惠金额（分）
     */
    private Integer discountAmount;

    /**
     * 服务费优惠抵扣金额（分）
     */
    private Integer serviceMoneyDiscount;

    /**
     * 时段数：0~32
     */
    private Integer sumPeriod;

    /**
     * 充电明细信息
     */
    private String chargeDetail;

    /**
     * 订单状态：0：未知，1：成功，2：失败
     */
    private Integer orderStatus;

    /**
     * 充电状态：1：启动中 2：充电中 3：停止中 4：已结束 5：未知
     */
    private Integer chargeStatus;

    /**
     * 充电停止原因：0：用户手动停止，1：运营平台停止，2：BMS停止，3：充电机器设备故障，4：连接器断开
     */
    private Integer stopReason;

    /**
     * 发票状态：0：未开票 1：已开票
     */
    private Integer invoiceStatus;

    /**
     * 发票id
     */
    private Integer invoiceId;
}
