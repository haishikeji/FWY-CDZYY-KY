package com.px.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.px.common.R;
import com.px.common.annotation.SysLog;
import com.px.entity.admin.queryParams.CommonQueryParam;
import com.px.entity.admin.queryParams.CustomChargeOrdersQueryParam;
import com.px.service.miniapp.ChargeOrderService;
import com.px.service.miniapp.PayLogService;
import com.px.service.miniapp.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关信息
 *
 * @author 品讯科技
 * @since 2023-08-18
 */
@RestController
@RequestMapping("/custom")
public class CustomController {

    private final UserService customUserService;
    private final ChargeOrderService customChargeOrders;
    private final PayLogService payLogService;

    public CustomController(UserService customUserService, ChargeOrderService customChargeOrders, PayLogService payLogService) {
        this.customUserService = customUserService;
        this.customChargeOrders = customChargeOrders;
        this.payLogService = payLogService;
    }

    @SaCheckPermission("account.list")
    @SysLog("查询用户列表")
    @GetMapping("/listUser")
    public R<?> listUser(@ModelAttribute CommonQueryParam params) {
        return R.success(customUserService.listCustomUser(params));
    }

    @SysLog("查询用户充电订单列表")
    @GetMapping("/listChargeOrders")
    public R<?> listChargeOrders(@ModelAttribute CustomChargeOrdersQueryParam params) {
        return R.success(customChargeOrders.customChargeOrders(params));
    }

    @SysLog("订单结算")
    @GetMapping("/closeChargeOrder/{startChargeSeq}")
    public R<?> closeChargeOrder(@PathVariable("startChargeSeq") String startChargeSeq) {
        customChargeOrders.closeChargeOrder(startChargeSeq);
        return R.success();
    }

    @SysLog(value = "用户充电订单列表导出", ignoreParams = true)
    @GetMapping("/exportChargeOrders")
    public void exportChargeOrders(@ModelAttribute CustomChargeOrdersQueryParam params, HttpServletResponse response) {
        customChargeOrders.exportCustomChargeOrders(params, response);
    }

    @SysLog("充值记录列表")
    @GetMapping("/listRecharge")
    public R<?> listRecharge(@ModelAttribute CommonQueryParam params) {
        return R.success(payLogService.listRecharge(params));
    }

}
