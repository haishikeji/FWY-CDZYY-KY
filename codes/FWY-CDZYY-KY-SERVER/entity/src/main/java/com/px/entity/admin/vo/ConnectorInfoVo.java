package com.px.entity.admin.vo;

import com.px.entity.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 充电桩接口（枪）信息
 * </p>
 *
 * @author 品讯科技
 * @since 2023-08-05
 */
@Data
@Accessors(chain = true)
public class ConnectorInfoVo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公司id
     */
    private Long companyId;

    /**
     * 站点id
     */
    private String stationId;

    /**
     * 站点编号
     */
    private String stationNo;

    /**
     * 站点名称
     */
    private String stationName;

    /**
     * 充电桩接口短编号
     */
    private String shortId;
    /**
     * 充电桩序列号
     */
    private String equipmentId;
    /**
     * 充电桩接口序列号
     */
    private String connectorId;
    /**
     * 充电桩名称
     */
    private String connectorName;
    /**
     * 充电设备接口类型：1家用插座（模式2）2：交流接口插座（模式3，连接方式B）3：交流接口插头（带枪线，模式3，连接方式C）4：直流接口枪头（带枪线，模式4）5：无线充电座6：其他
     */
    private Integer connectorType;
    /**
     * 额定电压上限（V）
     */
    private Integer voltageUpperLimits;
    /**
     * 额定电压下限（V）
     */
    private Integer voltageLowerLimits;
    /**
     * 额定电流
     */
    private Integer current;
    /**
     * 额定功率
     */
    private Double power;
    /**
     * 停车场车位编号
     */
    private String parkingNo;
    /**
     * 国家标准：1:2011 2:2015
     */
    private Integer nationalStandard;
    /**
     * 充电设备接口状态
     * 0：离网
     * 1：空闲
     * 2：占用（未充电）
     * 3：占用（充电中）
     * 4：占用（预约锁定）
     * 255：故障
     */
    private Integer status;

    public ConnectorInfoVo setShortId(String shortId) {
        this.shortId = shortId;
        this.stationNo = shortId.substring(0, 3);
        return this;
    }

}
