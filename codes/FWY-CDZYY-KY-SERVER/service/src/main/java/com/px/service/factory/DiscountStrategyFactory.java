package com.px.service.factory;

import com.px.entity.admin.Activity;
import com.px.service.admin.CouponService;
import com.px.service.miniapp.*;
import com.px.service.miniapp.impl.CouponDiscountHandle;
import com.px.service.miniapp.impl.NoDiscountHandle;
import com.px.service.miniapp.impl.RechargeRightsDiscountHandle;
import org.springframework.stereotype.Component;

/**
 * 优惠策略工厂
 *
 * @author 品讯科技
 */
@Component
public class DiscountStrategyFactory {

    private static OrderRechargeRightsService orderRechargeRightsService;
    private static UserRechargeRightsService userRechargeRightsService;
    private static OrderCouponService orderCouponService;
    private static UserCouponService userCouponService;
    private static CouponService couponService;

    public DiscountStrategyFactory(OrderRechargeRightsService orderRechargeRightsService, UserRechargeRightsService userRechargeRightsService, OrderCouponService orderCouponService, UserCouponService userCouponService, CouponService couponService) {
        DiscountStrategyFactory.orderRechargeRightsService = orderRechargeRightsService;
        DiscountStrategyFactory.userRechargeRightsService = userRechargeRightsService;
        DiscountStrategyFactory.orderCouponService = orderCouponService;
        DiscountStrategyFactory.userCouponService = userCouponService;
        DiscountStrategyFactory.couponService = couponService;
    }

    public static DiscountService getDiscountStrategy(String discountType) {
        if (Activity.DISCOUNT_TYPE_优惠券.equals(discountType)) {
            return new CouponDiscountHandle(orderCouponService, userCouponService, couponService);
        } else if (Activity.DISCOUNT_TYPE_服务费折扣权益.equals(discountType)) {
            return new RechargeRightsDiscountHandle(orderRechargeRightsService, userRechargeRightsService);
        } else {
            return new NoDiscountHandle();
        }
    }
}
