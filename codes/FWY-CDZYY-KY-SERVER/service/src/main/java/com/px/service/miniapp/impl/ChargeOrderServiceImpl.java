package com.px.service.miniapp.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.pagehelper.PageHelper;
import com.px.common.exception.BusinessException;
import com.px.common.utils.CommUtil;
import com.px.common.utils.HttpUtil;
import com.px.entity.admin.Activity;
import com.px.entity.admin.StationStatDay;
import com.px.entity.admin.StationStatMonth;
import com.px.entity.admin.queryParams.CustomChargeOrdersQueryParam;
import com.px.entity.admin.queryParams.StatQueryParam;
import com.px.entity.admin.vo.CustomOrderVo;
import com.px.entity.admin.vo.StationStatVo;
import com.px.entity.common.PageBean;
import com.px.entity.common.RedisKeys;
import com.px.entity.miniapp.ChargeOrder;
import com.px.entity.miniapp.queryParams.OrderQueryParams;
import com.px.entity.miniapp.vo.ChargeOrderVo;
import com.px.mapper.miniapp.ChargeOrderMapper;
import com.px.service.admin.ExportService;
import com.px.service.admin.StationStatDayService;
import com.px.service.admin.StationStatMonthService;
import com.px.service.cache.KymCache;
import com.px.service.miniapp.ChargeOrderService;
import com.px.service.miniapp.UserCouponService;
import com.px.service.miniapp.UserRechargeRightsService;
import com.px.service.mybatisplus.MyBaseServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * <p>
 * 充电订单表 服务实现类
 * </p>
 *
 * @author 品讯科技
 * @since 2023-08-08
 */
@Service
@DS("db-miniapp")
@Slf4j
public class ChargeOrderServiceImpl extends MyBaseServiceImpl<ChargeOrderMapper, ChargeOrder> implements ChargeOrderService {


    private final ExportService exportService;
    private final UserCouponService userCouponService;
    private final UserRechargeRightsService userRechargeRightsService;
    private final StationStatDayService stationStatDayService;
    private final StationStatMonthService stationStatMonthService;

    private final StringRedisTemplate redisTemplate;
    @Value("${en-plus.sassClose}")
    public String saasClose;


    public ChargeOrderServiceImpl(ExportService exportService, @Lazy UserCouponService userCouponService, UserRechargeRightsService userRechargeRightsService,
                                  StationStatDayService stationStatDayService, StationStatMonthService stationStatMonthService, StringRedisTemplate redisTemplate) {
        this.exportService = exportService;
        this.userCouponService = userCouponService;
        this.userRechargeRightsService = userRechargeRightsService;
        this.stationStatDayService = stationStatDayService;
        this.stationStatMonthService = stationStatMonthService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ChargeOrder getChargingOrderByUserId(Long userId) {
        return lambdaQuery()
                .eq(ChargeOrder::getUserId, userId)
                .eq(ChargeOrder::getOrderStatus, ChargeOrder.ORDER_STATUS_未知)
                .in(ChargeOrder::getChargeStatus, ChargeOrder.CHARGE_STATUS_预约中, ChargeOrder.CHARGE_STATUS_启动中, ChargeOrder.CHARGE_STATUS_充电中, ChargeOrder.CHARGE_STATUS_停止中)
                .one();
    }

    @Override
    public ChargeOrder getChargingOrderByStartChargeSeq(String startChargeSeq) {
        return lambdaQuery().eq(ChargeOrder::getStartChargeSeq, startChargeSeq).one();
    }

    @Override
    public PageBean<CustomOrderVo> customChargeOrders(CustomChargeOrdersQueryParam params) {
        if (params.getConnectorId() != null) {
            var connectorId = KymCache.INSTANCE.getConnectorId(params.getConnectorId());
            params.setConnectorId(connectorId);
        }
        // 判断数据权限
        if (params.getStationId() == null) {
            params.setStationId(CommUtil.isEmptyOrNull(KymCache.INSTANCE.getAdminUserStationIds(StpUtil.getLoginIdAsLong())) ? null : KymCache.INSTANCE.getAdminUserStationIds(StpUtil.getLoginIdAsLong()).get(0));
        }
        PageHelper.startPage(params.getPageNum(), params.getPageSize());
        // 查询订单列表（订单编号，手机号，充电站，充电桩SN/短码，订单状态，建单时间，结算时间，充电电量，实付金额，付款状态）
        var result = baseMapper.listChargeOrders(params);
        var page = new PageBean<>(result);
        page.setList(result.stream().map(item ->
                item.setShortId(KymCache.INSTANCE.getShortIdByEquipmentIdOrConnectorId(item.getConnectorId()))
                        .setStationName(KymCache.INSTANCE.getStationNameById(item.getStationId()))).collect(Collectors.toList()));
        var map = baseMapper.statChargeOrders(params);
        return page.setExtraData(map);
    }

    @SneakyThrows
    @Override
    public void exportCustomChargeOrders(CustomChargeOrdersQueryParam params, HttpServletResponse response) {

        if (params.getConnectorId() != null) {
            var connectorId = KymCache.INSTANCE.getConnectorId(params.getConnectorId());
            params.setConnectorId(connectorId);
        }
        // 判断数据权限
        var adminStationIds = KymCache.INSTANCE.getAdminUserStationIds(StpUtil.getLoginIdAsLong());
        if (params.getStationId() != null && adminStationIds.contains(params.getStationId())) {
            params.setStationId(adminStationIds.get(0));
        }
        // 查询订单列表（订单编号，手机号，充电站，充电桩SN/短码，订单状态，建单时间，结算时间，充电电量，实付金额，付款状态）
        var result = baseMapper.listChargeOrders(params);
        result = result.stream().map(item ->
                        item.setShortId(KymCache.INSTANCE.getShortIdByEquipmentIdOrConnectorId(item.getConnectorId()))
                                .setStationName(KymCache.INSTANCE.getStationNameById(item.getStationId())))
                .toList();

        // stationId,startChargeSeq,connectorId,startTime,endTime,totalPower,totalMoney,elecMoney,serviceMoney,orderStatus,chargeStatus,stopReason,invoiceStatus
        var rows = result.stream().map(item -> {
            var map = new HashMap<String, Object>();
            map.put("stationId", item.getStationId());
            map.put("startChargeSeq", item.getStartChargeSeq());
            map.put("connectorId", item.getConnectorId());
            map.put("startTime", item.getStartTime() == null ? "-" : item.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            map.put("endTime", item.getEndTime() == null ? "-" : item.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            map.put("totalPower", item.getTotalPower());
            map.put("totalMoney", BigDecimal.valueOf(item.getTotalMoney()).divide(BigDecimal.valueOf(100)));
            map.put("elecMoney", BigDecimal.valueOf(item.getElecMoney()).divide(BigDecimal.valueOf(100)));
            map.put("serviceMoney", BigDecimal.valueOf(item.getServiceMoney()).divide(BigDecimal.valueOf(100)));
            map.put("orderStatus", item.getOrderStatus());
            map.put("chargeStatus", item.getChargeStatus());
            return map;
        }).toList();

        // 通过工具类创建writer，默认创建xls格式
        ExcelWriter writer = ExcelUtil.getWriter();

        //自定义标题别名
        writer.addHeaderAlias("stationId", "站点ID");
        writer.addHeaderAlias("startChargeSeq", "充电订单号");
        writer.addHeaderAlias("connectorId", "充电设备接口编号");
        writer.addHeaderAlias("startTime", "充电开始时间");
        writer.addHeaderAlias("endTime", "充电结束时间");
        writer.addHeaderAlias("totalPower", "充电量（度）");
        writer.addHeaderAlias("totalMoney", "累积总金额（元）");
        writer.addHeaderAlias("elecMoney", "累积电费（元）");
        writer.addHeaderAlias("serviceMoney", "累积服务费（元）");
        writer.addHeaderAlias("orderStatus", "订单状态");
        writer.addHeaderAlias("chargeStatus", "充电状态");
        exportService.exportExcel("订单列表", writer, rows, response);
    }

    @Override
    public PageBean<ChargeOrderVo> listUserChargeOrders(OrderQueryParams params) {
        PageHelper.startPage(params.getPageNum(), params.getPageSize());
        var list = lambdaQuery()
                .eq(!CommUtil.isEmptyOrNull(params.getInvoiceStatus()), ChargeOrder::getInvoiceStatus, params.getInvoiceStatus())
                .eq(ChargeOrder::getUserId, StpUtil.getLoginIdAsLong()).orderByDesc(ChargeOrder::getId).list();
        var res = new PageBean<>(list);
        var voList = list.stream().map(chargeOrder -> {
            var vo = new ChargeOrderVo();
            BeanUtils.copyProperties(chargeOrder, vo);
            vo.setPayServiceAmount(vo.getServiceMoney() - vo.getDiscountAmount());
            return vo;
        }).toList();
        return new PageBean<>(voList).setList(res);
    }


    @Override
    public ChargeOrderVo orderDetailForApp(String startChargeSeq) {
        var chargeOrder = lambdaQuery().eq(ChargeOrder::getStartChargeSeq, startChargeSeq).one();
        // 订单对应的优惠信息
        var orderVo = new ChargeOrderVo();
        BeanUtils.copyProperties(chargeOrder, orderVo);
        orderVo.setStationName(KymCache.INSTANCE.getStationNameById(chargeOrder.getStationId()));
        // todo 修改成只显示优惠金额，具体优惠信息点击详情查看具体权益卡/优惠券
//        if (chargeOrder.getDiscountAmount() > 0) {
//            // 充值权益
//            var orderRechargeRight = orderRechargeRightsService.lambdaQuery().eq(OrderRechargeRights::getStartChargeSeq, startChargeSeq).one();
//            DynamicDataSourceContextHolder.push("db-admin");
//            var rechargeRights = rechargeRightsService.lambdaQuery().eq(RechargeRights::getId, orderRechargeRight.getRightsId()).one();
//            DynamicDataSourceContextHolder.poll();
//            var desc = rechargeRights.getRightsDesc();
//            orderVo.setRightsDesc(desc);
//        }
        return orderVo;
    }


    @Override
    public Object orderDiscountDetail(String startChargeSeq, String discountType) {
        return switch (discountType) {
            case Activity.DISCOUNT_TYPE_服务费折扣权益 ->
                    userRechargeRightsService.getUserOrderRechargeRights(startChargeSeq);
            case Activity.DISCOUNT_TYPE_优惠券 -> userCouponService.getUserOrderCoupon(startChargeSeq);
            default -> throw new BusinessException("未知的优惠类型");
        };
    }


    /**
     * 通过En+ sass关闭订单
     *
     * @param startChargeSeq
     */
    @Override
    public void closeChargeOrder(String startChargeSeq) {
        Headers headers = Headers.of("satoken", redisTemplate.opsForValue().get(RedisKeys.EN_PLUS_SASS_TOKEN));
        var res = HttpUtil.parseJson(HttpUtil.post(saasClose + startChargeSeq, headers));
        if (!(res.containsKey("success") && res.getBoolean("success"))) {
            log.error("关闭订单失败:{}", res);
            throw new BusinessException("关闭订单失败");
        }
    }


    /**
     * 站点统计
     *
     * @param params
     * @return
     */
    @Override
    public PageBean<StationStatVo> stationStat(StatQueryParam params) {
        // 判断数据权限
        var adminStationIds = KymCache.INSTANCE.getAdminUserStationIds(StpUtil.getLoginIdAsLong());
        if (params.getStationId() == null && !adminStationIds.isEmpty()) {
            params.setStationId(adminStationIds.get(0));
        }
        PageHelper.startPage(params.getPageNum(), params.getPageSize());
        var res = baseMapper.stationStat(params).stream().peek(item -> {
            item.setElecMoneyPercent((new BigDecimal(item.getElecMoney())).divide(new BigDecimal(Math.max(1, item.getTotalMoney())), 2, RoundingMode.HALF_UP));
            item.setServiceMoneyPercent(BigDecimal.ONE.subtract(item.getElecMoneyPercent()));
        }).collect(Collectors.toList());
        var map = Map.of(
                "totalPower", BigDecimal.valueOf(res.stream().mapToDouble(StationStatVo::getTotalPower).sum()).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                "serviceMoney", res.stream().mapToInt(StationStatVo::getServiceMoney).sum(),
                "payServiceAmount", res.stream().mapToInt(StationStatVo::getPayServiceAmount).sum()
        );
        return new PageBean<>(res).setExtraData(map);
    }

    /**
     * 站点统计详情
     *
     * @param params
     * @return
     */
    @Override
    @DS("db-admin")
    public Map<String, ?> stationStatDetail(StatQueryParam params) {

        CommUtil.asserts(CommUtil.isNotEmptyAndNull(params.getStationIds()), "站点不能为空");
        if (params.getType().equals(StatQueryParam.TYPE_DAY)) {
            return stationStatDayService.lambdaQuery()
                    .in(StationStatDay::getStationId, params.getStationIds())
                    .ge(StationStatDay::getStatDay, params.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .le(StationStatDay::getStatDay, params.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .orderByAsc(StationStatDay::getStatDay)
                    .list().stream().collect(Collectors.groupingBy(StationStatDay::getStationId, Collectors.toList()));
        } else {
            return stationStatMonthService.lambdaQuery()
                    .in(StationStatMonth::getStationId, params.getStationIds())
                    .ge(StationStatMonth::getStatMonth, params.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM")))
                    .le(StationStatMonth::getStatMonth, params.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM")))
                    .orderByAsc(StationStatMonth::getStatMonth)
                    .list().stream().collect(Collectors.groupingBy(StationStatMonth::getStationId, Collectors.toList()));
        }
    }

    /**
     * 充电站当日统计
     *
     * @return
     */
    @Override
    public Map<String, Object> stationTodayStat(String stationId) {
        if (CommUtil.null2Long(stationId) <= 0) {
            // 判断数据权限
            stationId = CommUtil.isEmptyOrNull(KymCache.INSTANCE.getAdminUserStationIds(StpUtil.getLoginIdAsLong())) ? null : KymCache.INSTANCE.getAdminUserStationIds(StpUtil.getLoginIdAsLong()).get(0);
        }

        return baseMapper.stationTodayStat(stationId);
    }

    /**
     * 订单号批量查询订单
     *
     * @param startChargeSeqs
     * @return
     */
    @Override
    public List<ChargeOrder> getChargeOrdersBySeqs(String[] startChargeSeqs) {
        return lambdaQuery().in(ChargeOrder::getStartChargeSeq, Arrays.stream(startChargeSeqs).toList()).list();
    }

}
