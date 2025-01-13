package com.px.entity.admin.vo;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.px.entity.BaseEntity;
import com.px.entity.admin.Activity;
import com.px.entity.admin.EquipmentInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 本地充电站信息
 * </p>
 *
 * @author 品讯科技
 * @since 2023-08-12
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LocalStationVo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公司id
     */
    private Long companyId;

    /**
     * 电站组id
     */
    private Long groupId;

    /**
     * en+充电站id
     */
    private String stationId;

    /**
     * en+运营商id
     */
    private String operatorId;

    /**
     * 设备所属运营平台组织机构代码
     */
    private String equipmentOwnerId;

    /**
     * 站点名称
     */
    private String stationName;

    /**
     * 充电中国家代码：CN
     */
    private String countryCode;

    /**
     * 充电站省市辖区编码
     */
    private String areaCode;

    /**
     * 地址
     */
    private String address;

    /**
     * 站点电话
     */
    private String stationTel;

    /**
     * 服务电话
     */
    private String serviceTel;

    /**
     * 站点类型：1：公共 50：个人 100：公交（专业）101：环卫（专用）102：物流（专用）103：出租车（专用）255：其他
     */
    private Integer stationType;

    /**
     * 站点状态：0：未知 1：建设中 5：关闭下线 6：维护中 50：正常使用
     */
    private Integer stationStatus;

    /**
     * 充电车位数量
     */
    private Integer parkingNum;

    /**
     * 充电桩位置坐标
     * <p>
     * FastjsonTypeHandler
     * 支持 MVC JSON 解析
     * 不支持 MySQL JSON 解析
     * <p>
     * JacksonTypeHandler
     * 支持 MVC JSON 解析
     * 支持 MySQL JSON 解析
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private JSONObject location;
    /**
     * 站点引导
     */
    private String siteGuide;
    /**
     * 建设场所：1：居民区 2：公共机构 3：企事业单位 4：写字楼 5：工业园区 6：交通枢纽 7：大型文体设施 8：城市绿地 9：大型建筑配建停车场 10：路边停车位 11：城际高速服务区 255：其他
     */
    private Integer construction;
    /**
     * 站点照片
     */
    private String pictures;
    /**
     * 使用车型描述
     */
    private String matchCars;
    /**
     * 车位楼层及数量描述
     */
    private String parkInfo;
    /**
     * 营业时间描述
     */
    private String businessHours;
    /**
     * 充电费描述
     */
    private String electricityFee;
    /**
     * 服务费率描述
     */
    private String serviceFee;
    /**
     * 停车费
     */
    private String parkFee;
    /**
     * 支付方式：刷卡、线上、现金（电子钱包类卡为刷卡、身份鉴权卡、微信/支付宝、APP为线上）
     */
    private String payment;
    /**
     * 是否支持预约：0：不支持 1：支持
     */
    private Integer supportOrder;
    /**
     * 备注
     */
    private String remark;

    /**
     * 充电设备信息列表
     */
    private List<EquipmentInfo> equipmentInfos;
    /**
     * 站点活动
     */
    private List<Activity> activityList;

}
