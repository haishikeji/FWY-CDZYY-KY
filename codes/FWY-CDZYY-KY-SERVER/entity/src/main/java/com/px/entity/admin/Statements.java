package com.px.entity.admin;

import com.baomidou.mybatisplus.annotation.TableName;
import com.px.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 客户对账单
 * </p>
 *
 * @author 品讯科技
 * @since 2023-12-27
 */
@Getter
@Setter
@TableName("t_statements")
@Accessors(chain = true)
public class Statements extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 客户用户id
     */
    private Long adminUserId;

    /**
     * 客户姓名
     */
    private String adminUserName;

    /**
     * 站点id
     */
    private String stationId;

    /**
     * 站点名称
     */
    private String stationName;

    /**
     * 统计时间（月）
     */
    private String statMonth;

    /**
     * 订单电量
     */
    private Double totalPower;

    /**
     * 实际抄表电量
     */
    private Double actualPower;

    /**
     * 电损电量
     */
    private Double elecLossPower;

    /**
     * 订单金额（分）
     */
    private Integer totalMoney;

    /**
     * 订单电费金额（分）
     */
    private Integer elecMoney;

    /**
     * 实际抄表电费金额（分）
     */
    private Integer actualElecMoney;

    /**
     * 电损电费金额（分）
     */
    private Integer elecLossMoney;

    /**
     * 服务费金额（分）
     */
    private Integer serviceMoney;

    /**
     * 优惠金额（分）
     */
    private Integer discountAmount;

    /**
     * 服务费优惠金额（分）
     */
    private Integer serviceMoneyDiscount;

    /**
     * 实际参与分成的服务费（分）
     */
    private Integer actualServiceMoney;

    /**
     * 分成比例 0.45表示45%
     */
    private Double splittingProportion;

    /**
     * 分成金额（分）
     */
    private Integer splittingAmount;

    /**
     * 电损承担比例 0.30代表30%
     */
    private Double elecLossProportion;

    /**
     * 电损承担金额（分）
     */
    private Integer elecLossAmount;

    /**
     * 增值税率 0.06表示6%
     */
    private Double vatRate;

    /**
     * 增值税额（分）
     */
    private Integer vatAmount;

    /**
     * 实际分成金额
     */
    private Integer actualSplittingAmount;

    /**
     * 状态：0-无效，1-有效
     */
    private Byte status;

    /**
     * 备注
     */
    private String remark;
}
