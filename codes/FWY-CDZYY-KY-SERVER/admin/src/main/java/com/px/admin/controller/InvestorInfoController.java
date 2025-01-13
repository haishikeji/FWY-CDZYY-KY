package com.px.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.px.common.R;
import com.px.common.utils.CommUtil;
import com.px.entity.admin.InvestorInfo;
import com.px.entity.admin.queryParams.CommonQueryParam;
import com.px.service.admin.InvestorInfoService;
import com.px.service.cache.KymCache;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 投资者-物业信息表 前端控制器
 * </p>
 *
 * @author 品讯科技
 * @since 2023-12-27
 */
@RestController
@RequestMapping("/investorInfo")
public class InvestorInfoController {

    private final InvestorInfoService investorInfoService;

    public InvestorInfoController(InvestorInfoService investorInfoService) {
        this.investorInfoService = investorInfoService;
    }

    /**
     * 新增
     *
     * @param investorInfo
     */
    @SaCheckPermission("investor.add")
    @PostMapping("/create")
    R<?> create(@RequestBody InvestorInfo investorInfo) {
        investorInfo.setId(null);
        investorInfo.setStationName(KymCache.INSTANCE.getStationNameById(investorInfo.getStationId()));
        CommUtil.asserts(investorInfo.getAdminUserId() != null, "请选择关联客户");
        return R.success(investorInfoService.save(investorInfo));
    }

    /**
     * 修改投资者/物业信息
     * @param investorInfo
     * @return
     */
    @PostMapping("/update")
    R<?> update(@RequestBody InvestorInfo investorInfo) {
        investorInfo.setStationName(KymCache.INSTANCE.getStationNameById(investorInfo.getStationId()));
        CommUtil.asserts(investorInfo.getAdminUserId() != null, "请选择关联客户");
        return R.success(investorInfoService.updateById(investorInfo));
    }

    /**
     * 获取单个投资者/物业信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    R<?> getInvestorInfoById(@PathVariable("id") Long id) {
        return R.success(investorInfoService.getById(id));
    }

    /**
     * 投资者/物业信息列表
     * @param params
     * @return
     */
    @GetMapping("/list")
    R<?> list(@ModelAttribute CommonQueryParam params) {
        return R.success(investorInfoService.list(params));
    }

}
