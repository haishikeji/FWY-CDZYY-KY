package com.px.miniapp.controller;

import com.px.common.R;
import com.px.service.admin.BannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * banner配置表 前端控制器
 * </p>
 *
 * @author 品讯科技
 * @since 2023-10-18
 */
@RestController
@RequestMapping("/banner")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * Banner列表
     *
     * @return
     */
    @GetMapping
    R<?> listBanner() {
        return R.success(bannerService.listBannerForApp());
    }

}
