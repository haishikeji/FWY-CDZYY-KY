package com.px.entity.enplus;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author 品讯科技
 * @description 充电设备信息
 * @date 2024-08
 */
@Data
@Accessors(chain = true)
public class EnEquipmentInfo {

    /**
     * 设备编码
     */
    @JSONField(name = "EquipmentID")
    private String equipmentId;

    /**
     * 充电桩短ID
     */
    private String shortId;

    /**
     * 车位编号
     */
    private String parkingNo;

    /**
     * 设备生产商组织机构代码
     */
    @JSONField(name = "ManufacturerID")
    private String manufacturerId;

    /**
     * 设备生产商名称
     */
    @JSONField(name = "ManufacturerName")
    private String manufacturerName;

    /**
     * 设备型号
     */
    @JSONField(name = "EquipmentModel")
    private String equipmentModel;

    /**
     * 设备生产日期(YYYY-MM-DD)
     */
    @JSONField(name = "ProductionDate")
    private String productionDate;

    /**
     * 设备类型
     * 1：直流设备
     * 2：交流设备
     * 3：交直流一体设备
     * 4：无线设备
     * 5：其他
     */
    @JSONField(name = "EquipmentType")
    private Integer equipmentType;

    /**
     * 充电设备接口信息列表
     */
    private List<EnConnectorInfo> connectorInfos;

    /**
     * 充电桩坐标
     */
    private JSONObject location;

    /**
     * 充电设备总功率
     */
    @JSONField(name = "Power")
    private Double power;

    @JSONCreator
    public EnEquipmentInfo(@JSONField(name = "ConnectorInfos") JSONArray array,
                           @JSONField(name = "EquipmentLng") float equipmentLng,
                           @JSONField(name = "EquipmentLat") float equipmentLat) {
        this.connectorInfos = array.toJavaList(EnConnectorInfo.class);
        this.location = JSONObject.of("equipmentLng", equipmentLng, "equipmentLat", equipmentLat);
    }


}
