package com.dobbinsoft.gus.remotesales.service.biz;

import com.dobbinsoft.gus.common.model.constant.HeaderConstants;
import com.dobbinsoft.gus.common.utils.context.GenericRequestContextHolder;
import com.dobbinsoft.gus.common.utils.context.bo.*;
import com.dobbinsoft.gus.remotesales.data.enums.OrderLogTypeEnum;
import com.dobbinsoft.gus.remotesales.data.enums.OrderStatusEnum;
import com.dobbinsoft.gus.remotesales.data.enums.OrderTypeEnum;
import com.dobbinsoft.gus.remotesales.data.enums.PayStatusEnum;
import com.dobbinsoft.gus.remotesales.data.po.OrderLogPO;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import com.dobbinsoft.gus.remotesales.mapper.OrderLogMapper;
import com.dobbinsoft.gus.remotesales.mapper.OrderMapper;
import com.dobbinsoft.gus.remotesales.utils.delay.SchedulerTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class SchedulerBizService {

    public static final String ORDER_AUTO_CANCEL = "ORDER_AUTO_CANCEL";

//    public static final String ORDER_RECEIPT_CONFIRM_NOTICE = "ORDER_RECEIPT_CONFIRM_NOTICE";

    public static final String ORDER_RECEIPT_AUTO_CONFIRM = "ORDER_RECEIPT_AUTO_CONFIRM";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private NotificationBizService notificationBizService;
    @Autowired
    private OrderLogMapper orderLogMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String ORDER_TASK_CREATED_KEY = "ORDER_TASK_CREATED";

    /**
     * 自动取消订单
     * @param orderId
     * @param tenantId
     */
    @SchedulerTarget(name = ORDER_AUTO_CANCEL)
    public void onOrderCancel(Long orderId, String tenantId) {
        try {
            // 手动设置租户信息
            RequestProperty requestProperty = RequestProperty.builder()
                    .tenantContext(new TenantContext())
                    .traceContext(new TraceContext())
                    .identityContext(new IdentityContext())
                    .languageContext(new LanguageContext())
                    .build();
            requestProperty.setProperty(HeaderConstants.TENANT_ID.name(), tenantId);
            requestProperty.initContext();
            GenericRequestContextHolder.setRequestProperty(requestProperty);
            OrderPO orderPO = orderMapper.selectById(orderId);
            if (orderPO == null) {
                // 见于手工删数据
                log.warn("job run ORDER_AUTO_CANCEL STOP: orderId:{}", orderId);
                return;
            }
            if (!OrderStatusEnum.TO_PAY.getCode().equals(orderPO.getStatus()) || PayStatusEnum.PAID.getCode().equals(orderPO.getPayStatus()) && OrderTypeEnum.ORDER.getCode().equals(orderPO.getType())) {
                log.info("job run ORDER_AUTO_CANCEL stop: orderId:{},orderStatus:{},payStatus:{},orderType:{}", orderId, orderPO.getStatus(), orderPO.getPayStatus(), orderPO.getType());
                return;
            }

            orderPO.setStatus(OrderStatusEnum.EXPIRED.getCode());
            orderMapper.updateById(orderPO);

            //   订单日志
            OrderLogPO orderLog = OrderLogPO.builder()
                    .orderId(orderPO.getId())
                    .orderNo(orderPO.getOrderNo())
                    .status(OrderStatusEnum.EXPIRED.getCode())
                    .statusDesc(OrderStatusEnum.EXPIRED.getMsg())
                    .type(OrderLogTypeEnum.ORDER_EXPIRED.getCode())
                    .typeDesc(OrderLogTypeEnum.ORDER_EXPIRED.getMsg())
                    .build();
            orderLogMapper.insert(orderLog);
            log.info("job ORDER_AUTO_CANCEL run success: orderId:{}", orderId);
        } catch (Exception e) {
            log.error("job ORDER_AUTO_CANCEL run error: orderId:{}", orderId, e);
        }
    }

//    /**
//     * 提醒 确认收货
//     * @param orderId
//     * @param deliveryMethod
//     * @param tenantId
//     */
//    @SchedulerTarget(name = ORDER_RECEIPT_CONFIRM_NOTICE)
//    public void onOrderReceiptNotice(Long orderId, Integer deliveryMethod, String tenantId) {
//        try {
//            // 手动设置租户信息
//            RequestProperty requestProperty = RequestProperty.builder()
//                    .tenantContext(new TenantContext())
//                    .traceContext(new TraceContext())
//                    .identityContext(new IdentityContext())
//                    .languageContext(new LanguageContext())
//                    .build();
//            requestProperty.setProperty(HeaderConstants.TENANT_ID.name(), tenantId);
//            requestProperty.initContext();
//            GenericRequestContextHolder.setRequestProperty(requestProperty);
//            OrderPO orderPO = orderMapper.selectById(orderId);
//            if (orderPO == null) {
//                log.warn("job run ORDER_RECEIPT_CONFIRM_NOTICE STOP: orderId:{}", orderId);
//                return;
//            }
//            if (!OrderStatusEnum.TO_RECEIVE.getCode().equals(orderPO.getStatus()) && OrderTypeEnum.ORDER.getCode().equals(orderPO.getType()) && Objects.equals(orderPO.getDeliveryMethod(), deliveryMethod)) {
//                log.info("job run ORDER_RECEIPT_CONFIRM_NOTICE STOP: orderId:{},orderStatus:{},payStatus:{},orderType:{},order-deliveryMethod:{},params-deliveryMethod:{}", orderId, orderPO.getStatus(), orderPO.getPayStatus(), orderPO.getType(), orderPO.getDeliveryMethod(), deliveryMethod);
//                return;
//            }
//            notificationBizService.sendCustomerNotConfirmedReceiptMsg(orderPO);
//            log.info("job ORDER_RECEIPT_CONFIRM_NOTICE run SUCCESS: orderId:{}", orderId);
//        } catch (Exception e) {
//            log.error("job ORDER_RECEIPT_CONFIRM_NOTICE run error: orderId:{}", orderId, e);
//        }
//    }

    /**
     * 自动确认收货
     * @param orderId
     * @param deliveryMethod
     * @param tenantId
     */
    @SchedulerTarget(name = ORDER_RECEIPT_AUTO_CONFIRM)
    public void onOrderAutoReceipt(Long orderId, Integer deliveryMethod, String tenantId) {
        try {
            // 手动设置租户信息
            RequestProperty requestProperty = RequestProperty.builder()
                    .tenantContext(new TenantContext())
                    .traceContext(new TraceContext())
                    .identityContext(new IdentityContext())
                    .build();
            requestProperty.setProperty(HeaderConstants.TENANT_ID.name(), tenantId);
            requestProperty.initContext();
            GenericRequestContextHolder.setRequestProperty(requestProperty);
            OrderPO orderPO = orderMapper.selectById(orderId);
            if (orderPO == null) {
                log.warn("job run ORDER_AUTO_CONFIRM STOP: orderId:{}", orderId);
                return;
            }
            if (!OrderStatusEnum.TO_RECEIVE.getCode().equals(orderPO.getStatus()) && OrderTypeEnum.ORDER.getCode().equals(orderPO.getType()) && Objects.equals(orderPO.getDeliveryMethod(), deliveryMethod)) {
                log.info("job run ORDER_RECEIPT_AUTO_CONFIRM STOP: orderId:{},orderStatus:{},payStatus:{},orderType:{},order-deliveryMethod:{},params-deliveryMethod:{}", orderId, orderPO.getStatus(), orderPO.getPayStatus(), orderPO.getType(), orderPO.getDeliveryMethod(), deliveryMethod);
                return;
            }

            orderPO.setStatus(OrderStatusEnum.COMPLETED.getCode());
            orderMapper.updateById(orderPO);
            //   订单日志
            OrderLogPO orderLog = OrderLogPO.builder()
                    .orderId(orderPO.getId())
                    .orderNo(orderPO.getOrderNo())
                    .status(OrderStatusEnum.COMPLETED.getCode())
                    .statusDesc(OrderStatusEnum.COMPLETED.getMsg())
                    .type(OrderLogTypeEnum.AUTO_RECEIPT.getCode())
                    .typeDesc(OrderLogTypeEnum.AUTO_RECEIPT.getMsg())
                    .build();
            orderLogMapper.insert(orderLog);
            notificationBizService.sendCustomerConfirmedReceiptMsg(orderPO);
            redisTemplate.opsForValue().setBit(ORDER_TASK_CREATED_KEY, orderId.intValue(), Boolean.FALSE);
            log.info("job ORDER_RECEIPT_AUTO_CONFIRM run SUCCESS: orderId:{}", orderId);
        } catch (Exception e) {
            log.error("job ORDER_RECEIPT_AUTO_CONFIRM run error: orderId:{}", orderId, e);
        }
    }
}
