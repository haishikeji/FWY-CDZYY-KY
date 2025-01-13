package com.px.entity.admin.queryParams;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.px.entity.common.PageParams;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * @author 品讯科技
 * @description 运营后台发票查询参数
 * @date 2024-08
 */
@Data
public class InvoiceQueryParam extends PageParams {
    /**
     * id
     */
    private Long id;
    /**
     * 发票抬头
     */
    private String invoiceTitle;
    /**
     * 发票状态
     */
    private Integer status;

    /**
     * 开票人
     */
    private String biller;

    /**
     * 公司税号
     */
    private String taxId;

    /**
     * 发票类型:INDIVIDUAL-个人 ORGANIZATION-企业
     */
    private String invoiceType;

    /**
     * 接收发票邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 开票时间（开始结束）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
}
