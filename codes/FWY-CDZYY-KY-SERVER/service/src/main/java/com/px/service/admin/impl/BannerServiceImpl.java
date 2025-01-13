package com.px.service.admin.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.px.common.utils.CommUtil;
import com.px.common.utils.IDGenerator;
import com.px.entity.admin.Banner;
import com.px.entity.admin.queryParams.BannerQueryParam;
import com.px.entity.common.PageBean;
import com.px.mapper.admin.BannerMapper;
import com.px.service.admin.BannerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * <p>
 * banner配置表 服务实现类
 * </p>
 *
 * @author 品讯科技
 * @since 2023-10-18
 */
@Service
@DS("db-admin")
public class BannerServiceImpl extends MPJBaseServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Banner createBanner(Banner banner) {
        banner.setId(IDGenerator.INS().nextId());
        save(banner);
        return banner;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Banner updateBanner(Banner banner) {
        lambdaUpdate()
                .set(!CommUtil.isEmptyOrNull(banner.getBannerDesc()), Banner::getBannerDesc, banner.getBannerDesc())
                .set(!CommUtil.isEmptyOrNull(banner.getBannerUrl()), Banner::getBannerUrl, banner.getBannerUrl())
                .set(!CommUtil.isEmptyOrNull(banner.getLinkUrl()), Banner::getLinkUrl, banner.getLinkUrl())
                .set(banner.getStartTime() != null, Banner::getStartTime, banner.getStartTime())
                .set(banner.getEndTime() != null, Banner::getEndTime, banner.getEndTime())
                .set(banner.getStatus() != null, Banner::getStatus, banner.getStatus())
                .set(!CommUtil.isEmptyOrNull(banner.getRemark()), Banner::getRemark, banner.getRemark())
                .set(CommUtil.isNotEmptyAndNull(banner.getActivityId()), Banner::getActivityId, banner.getActivityId())
                .eq(Banner::getId, banner.getId()).update();
        return banner;
    }

    @Override
    public PageBean<Banner> listBanner(BannerQueryParam params) {
        var list = lambdaQuery()
                .eq(params.getStatus() != null, Banner::getStatus, params.getStatus())
                .gt(params.getStartTime() != null, Banner::getStartTime, params.getStartTime())
                .lt(params.getEndTime() != null, Banner::getStartTime, params.getEndTime())
                .like(!CommUtil.isEmptyOrNull(params.getBannerDesc()), Banner::getBannerDesc, params.getBannerDesc())
                .list();
        return new PageBean<>(list);
    }

    @Override
    public List<Banner> listBannerForApp() {
        return lambdaQuery().eq(Banner::getStatus, Banner.STATUS_有效).list();
    }

}
