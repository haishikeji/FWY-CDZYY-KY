package com.px.miniapp.jobs;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.px.common.constant.ResponseEnum;
import com.px.common.exception.BusinessException;
import com.px.common.utils.CommUtil;
import com.px.entity.miniapp.ChargeOrder;
import com.px.entity.miniapp.delay.DelayChargeOrder;
import com.px.service.jobs.DelayService;
import com.px.service.jobs.DelayedItem;
import com.px.service.miniapp.ChargeOrderService;
import com.px.service.miniapp.ChargeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 品讯科技
 * @description 启动充电延迟任务（预约充电）
 * @date 2024-08
 */
@Component("StartChargeDelayJob")
@Slf4j
@DS("db-miniapp")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON) // 设置成单例
public class StartChargeDelayJob implements DelayService<DelayChargeOrder> {

    /**
     * 预约订单队列
     */
    private final static DelayQueue<DelayedItem<DelayChargeOrder>> START_DELAY_QUEUE = new DelayQueue<>();
    private final ChargeOrderService chargeOrderService;
    private final ChargeService chargeService;
    /**
     * 线程池
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * 重试列表
     */
    private ArrayList<String> retryList = new ArrayList<>();

    public StartChargeDelayJob(ChargeOrderService chargeOrderService, ChargeService chargeService) {
        this.chargeOrderService = chargeOrderService;
        this.chargeService = chargeService;
    }

    @DS("db-miniapp")
    // 这里不能使用@PostConstruct，在初始化完成后, bean 进入增强阶段, 所以这个阶段的任何AOP都是无效的,https://www.cnblogs.com/eternityz/p/15330069.html
    @EventListener(classes = {ContextRefreshedEvent.class}, id = "StartChargeDelayJob")
    @Async
    public void init() {
        // 队列加载所有充电状态为预约中的订单，按照开始时间排序
        var orderList = chargeOrderService.lambdaQuery()
                .eq(ChargeOrder::getChargeStatus, ChargeOrder.CHARGE_STATUS_预约中)
                .eq(ChargeOrder::getIsBooking, ChargeOrder.IS_BOOKING_是)
                .orderByAsc(ChargeOrder::getStartTime)
                .list();
        var delayChargeOrderList = orderList.stream().map(o -> new DelayChargeOrder()
                        .setStartChargeSeq(o.getStartChargeSeq())
                        .setUserId(o.getUserId())
                        .setConnectorId(o.getConnectorId())
                        .setStartTime(o.getStartTime())
                        .setEndTime(o.getEndTime())
                        .setChargeStatus(o.getChargeStatus()))
                .toList();
        var delayList = delayChargeOrderList.stream().map(delay -> new DelayedItem<>(delay, delay.getStartTime())).toList();
        START_DELAY_QUEUE.addAll(delayList);
        // 开启线程处理队列消息
        processDelayedOrders();
    }

    private void processDelayedOrders() {
        while (true) {
            executor.execute(() -> {
                ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> null); // 初始化为空值，避免使用new ThreadLocal()
                log.info("预约启动充电处理线程：{}", Thread.currentThread().getName());
                DelayedItem<DelayChargeOrder> delayedItem = null;
                try {
                    delayedItem = START_DELAY_QUEUE.take();
                    log.info("出队预约充电订单：{}，队列剩余：{}", delayedItem.data.getStartChargeSeq(), START_DELAY_QUEUE.size());
                    // 启动充电
                    var order = delayedItem.data;
                    chargeService.queryStartCharge(order.getUserId(), order.getConnectorId(), null, null,false, null, null);
                    log.info("预约充电启动成功:用户:{}，订单号：{}，预约启动时间：{}", order.getUserId(), order.getStartChargeSeq(), order.getStartTime());
                    // 线程休眠250ms
                    Thread.sleep(250);
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        log.error("预约充电队列take异常", e);
                    } else {
                        log.info("预约启动充电失败，订单号：{}", threadLocal.get());
                        if (e instanceof BusinessException && (ResponseEnum.EN_PLUS_TOKEN_EXCEPTION.getCode().equals(((BusinessException) e).getCode()))) {
                            if (retryList.contains(threadLocal.get())) {
                                log.info("EN+ token异常，预约订单:{}已重试忽略", threadLocal.get());
                                log.error(e.getMessage());
                                // 启动失败将订单状态修改为充电状态已结束，订单状态已确认，结束原因：预约启动失败
                                updateOrderStatus(threadLocal.get(), ChargeOrder.CHARGE_STATUS_已结束, ChargeOrder.ORDER_STATUS_失败, ChargeOrder.STOP_REASON_预约启动充电失败);
                                retryList.remove(threadLocal.get());
                                return;
                            }
                            log.info("EN+ token异常，预约订单：{}，重试", threadLocal.get());
                            // token异常就重新放入队列重试
                            var success = addToDelayQueue(delayedItem);
                            if (success) {
                                retryList.add(threadLocal.get());
                            }
                            // 跳出本次循环
                            return;
                        }
                        log.error(e.getMessage());
                        // 启动失败将订单状态修改为充电状态已结束，订单状态已确认，结束原因：预约启动失败
                        updateOrderStatus(threadLocal.get(), ChargeOrder.CHARGE_STATUS_已结束, ChargeOrder.ORDER_STATUS_失败, ChargeOrder.STOP_REASON_预约启动充电失败);
                    }
                } finally {
                    threadLocal.remove();
                }
            });
            if (!executor.isTerminated()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error("Delay queue processing interrupted.", e);
                    return;
                }
            }
        }
    }

    private void updateOrderStatus(String startChargeSeq, int chargeStatus, int orderStatus, int stopReason) {
        chargeOrderService.lambdaUpdate()
                .eq(ChargeOrder::getStartChargeSeq, startChargeSeq)
                .set(ChargeOrder::getChargeStatus, chargeStatus)
                .set(ChargeOrder::getOrderStatus, orderStatus)
                .set(ChargeOrder::getStopReason, stopReason)
                .update();
    }

    @Override
    public boolean addToDelayQueue(DelayedItem<DelayChargeOrder> delayedItem) {
        return START_DELAY_QUEUE.add(delayedItem);
    }

    @Override
    public boolean addToDelayQueue(DelayChargeOrder delayChargeOrder) {
        DelayedItem<DelayChargeOrder> orderDelayed = new DelayedItem<>(delayChargeOrder, delayChargeOrder.getStartTime());
        return START_DELAY_QUEUE.add(orderDelayed);
    }

    @Override
    public boolean removeFromDelayQueue(Object startChargeSeq) {
        if (CommUtil.isEmptyOrNull(startChargeSeq)) {
            return false;
        }
        return START_DELAY_QUEUE.removeIf(queue -> queue.data.getStartChargeSeq().equals(startChargeSeq));
    }
}
