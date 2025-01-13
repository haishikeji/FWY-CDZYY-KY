package com.px.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.px.entity.admin.InvestorInfo;
import com.px.entity.admin.queryParams.CommonQueryParam;
import com.px.entity.common.PageBean;


/**
 * <p>
 * 投资者-物业信息表 服务类
 * </p>
 *
 * @author 品讯科技
 * @since 2023-12-27
 */
public interface InvestorInfoService extends IService<InvestorInfo> {

    PageBean<InvestorInfo> list(CommonQueryParam params);
}
