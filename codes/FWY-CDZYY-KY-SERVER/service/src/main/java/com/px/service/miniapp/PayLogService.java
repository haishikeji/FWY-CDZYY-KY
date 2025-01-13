package com.px.service.miniapp;

import com.github.yulichang.base.MPJBaseService;
import com.px.entity.admin.queryParams.CommonQueryParam;
import com.px.entity.admin.vo.CustomRechargeVo;
import com.px.entity.common.PageBean;
import com.px.entity.miniapp.PayLog;

/**
 * <p>
 * 支付日志 服务类
 * </p>
 *
 * @author 品讯科技
 * @since 2023-08-12
 */
public interface PayLogService extends MPJBaseService<PayLog> {

    PageBean<CustomRechargeVo> listRecharge(CommonQueryParam params);

}
