package com.dobbinsoft.gus.remotesales.service.biz;

import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.client.wecom.WeComAdapterClient;
import com.dobbinsoft.gus.remotesales.data.po.OrderAdjustPricePO;
import com.dobbinsoft.gus.remotesales.data.po.OrderItemPO;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.MessageSendResponse;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.WxCpMessage;
import com.dobbinsoft.gus.remotesales.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class NotificationBizService {

    @Autowired
    private WeComAdapterClient weComAdapterClient;
    @Autowired
    private ConfigCenterClient configCenterClient;

    private static final String TEXT_CARD = "textcard";
    private static final String TEXT = "text";
    private static final String PRICE_FORMAT = "###,###,##0.00";
    private static final String CUSTOMER_NAME_LABEL = "客户姓名: ";
    private static final String ORDER_NO_LABEL = "订单编号: ";
    private static final String PAY_TIME_LABEL = "支付时间: ";
    private static final String PAY_AMOUNT_LABEL = "支付金额: ￥";
    private static final String REFUND_AMOUNT_LABEL = "退款金额: ￥";
    private static final String BTN_DETAIL = "详情";
    private static final String ORDER_DETAIL_PATH = "/#/sa/order?orderNo=";
    private static final String ADJUST_PRICE_PATH = "/#/adjust-price?orderNo=%s&id=%s";
    private static final String PRODUCT_NAME_LABEL = "商品名: ";
    private static final String ORIGINAL_PRICE_LABEL = "原价: ￥";
    private static final String ADJUST_PRICE_LABEL = "调价价格: ￥";
    private static final String ADJUST_PRICE_REMARK = "调价原因: ";
    private static final String REQUEST_TIME_LABEL = "请求时间: ";

//    public void sendRemindmsg(String orderNo, String saName, Integer number, String storeName, String wwid, Boolean storeManagement) {
//        if (StringUtils.isEmpty(wwid)) {
//            log.warn("POS单推送 失败 wwid为空 wwid: {}, storeManagement:{}", wwid, storeManagement);
//            return;
//        }
//
//        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
//
//        StringBuilder message = new StringBuilder();
//        message.append("销售：").append(saName).append("\n");
//        message.append("门店：").append(storeName).append("\n");
//
//        String page;
//        if (storeManagement) {
//            message.append("描述: 你的店铺有").append(number).append("个订单还未录入POS单号，请提醒销售今日及时录入\n");
//            page = "front/store-manager/orders";
//        } else {
//            message.append("描述: 你的店铺有").append(number).append("个订单还未录入POS单号，请于今日及时录入\n");
//            page = "front/sa/orders";
//        }
//
//        try {
//            WxCpMessage wxCpMessage = new WxCpMessage();
//            wxCpMessage.setAgentId(Integer.parseInt(configContentVO.getBrand().getAgentId()));
//            wxCpMessage.setToUser(wwid);
//            wxCpMessage.setMsgType(TEXT_CARD);
//            wxCpMessage.setTitle("录入提醒");
//            wxCpMessage.setDescription(message.toString());
//            wxCpMessage.setUrl(page);
//            wxCpMessage.setBtnTxt("更多");
//            MessageSendResponse response = weComAdapterClient.messageSend(configContentVO.getBrand().getAgentId(), wxCpMessage);
//            if (response != null && response.getErrcode() == 0) {
//                log.info("POS单推送成功 - 订单号: {}, 接收人: {}", orderNo, wwid);
//            } else {
//                log.error("POS单推送失败 - 订单号: {}, 接收人: {}, 错误码: {}, 错误信息: {}",
//                        orderNo, wwid,
//                        response != null ? response.getErrcode() : "null",
//                        response != null ? response.getErrmsg() : "null");
//            }
//        } catch (Exception e) {
//            log.error("POS单推送失败 - 订单号: {}, 接收人: {}, 错误: {}", orderNo, wwid, e.getMessage(), e);
//        }
//    }

    public void sendRefundMsg(OrderPO order,
                              String title,
                              BigDecimal refundAmount,
                              String comment,
                              List<String> sendUserIds) {
        if (CollectionUtils.isEmpty(sendUserIds)) {
            log.info("发送名单为空---");
            return;
        }

        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        String page = configContentVO.getBrand().getPageBaseUrl() + ORDER_DETAIL_PATH + order.getOrderNo();
        DecimalFormat usFormat = new DecimalFormat(PRICE_FORMAT);
        StringBuilder message = new StringBuilder();
        message.append("销售: ").append(order.getCaName()).append("\n");
        message.append("门店: ").append(order.getStoreName()).append("\n");
        message.append("订单号: ").append(order.getOrderNo()).append("\n");
        message.append("退款金额: ").append(usFormat.format(refundAmount));

        if (StringUtils.isNotEmpty(comment)) {
            message.append("\n").append(comment);
        }

        String toUsers = String.join("|", sendUserIds);
        WxCpMessage wxCpMessage = new WxCpMessage();
        wxCpMessage.setAgentId(Integer.parseInt(configContentVO.getBrand().getAgentId()));
        wxCpMessage.setToUser(toUsers);
        wxCpMessage.setMsgType(TEXT_CARD);
        wxCpMessage.setTitle(title);
        wxCpMessage.setDescription(message.toString());
        wxCpMessage.setUrl(page);
        wxCpMessage.setBtnTxt("更多");

        MessageSendResponse response = weComAdapterClient.messageSend(configContentVO.getBrand().getAgentId(), wxCpMessage);
        if (response != null && response.getErrcode() == 0) {
            log.info("退款消息推送成功 - 订单号: {}, 接收人: {}", order.getOrderNo(), toUsers);
        } else {
            log.error("退款消息推送失败 - 订单号: {}, 接收人: {}, 错误码: {}, 错误信息: {}",
                    order.getOrderNo(), toUsers,
                    response != null ? response.getErrcode() : "null",
                    response != null ? response.getErrmsg() : "null");
        }
//        for (String userId : sendUserIds) {
//            try {
//            } catch (Exception e) {
//                log.error("退款消息推送失败 - 订单号: {}, 接收人: {}, 错误: {}", order.getOrderNo(), userId, e.getMessage(), e);
//            }
//        }
    }

    public void sendPriceAdjustmentAudit(String title, OrderPO orderPO, OrderItemPO orderItemPO, OrderAdjustPricePO orderAdjustPricePO, String toUser) {
        if (StringUtils.isEmpty(toUser)) {
            log.info("调价审核通知推送失败 - 订单号: {}, 接收人为空 - 门店未设置店长", orderPO.getOrderNo());
            return;
        }
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        String page = configContentVO.getBrand().getPageBaseUrl() + ADJUST_PRICE_PATH.formatted(orderPO.getOrderNo(), orderItemPO.getId());
        DecimalFormat usFormat = new DecimalFormat(PRICE_FORMAT);
        StringBuilder message = new StringBuilder();
        
        // Get product list from order items

        message.append(PRODUCT_NAME_LABEL).append(orderItemPO.getProductName()).append("\n");
        message.append(ORIGINAL_PRICE_LABEL).append(usFormat.format(orderAdjustPricePO.getOriginalPrice())).append("\n");
        message.append(ADJUST_PRICE_LABEL).append(usFormat.format(orderAdjustPricePO.getPrice())).append("\n");
        message.append(ADJUST_PRICE_REMARK).append(Objects.nonNull(orderAdjustPricePO.getInnerRemark())?orderAdjustPricePO.getInnerRemark():"").append("\n");
        message.append(REQUEST_TIME_LABEL).append(DateUtils.localDateTimeToString(orderPO.getCreatedTime().toLocalDateTime()));

        try {
            WxCpMessage wxCpMessage = new WxCpMessage();
            wxCpMessage.setAgentId(Integer.parseInt(configContentVO.getBrand().getAgentId()));
            wxCpMessage.setToUser(toUser);
            wxCpMessage.setMsgType(TEXT_CARD);
            wxCpMessage.setTitle(title);
            wxCpMessage.setDescription(message.toString());
            wxCpMessage.setUrl(page);
            wxCpMessage.setBtnTxt("更多");

            MessageSendResponse response = weComAdapterClient.messageSend(configContentVO.getBrand().getAgentId(), wxCpMessage);
            if (response != null && response.getErrcode() == 0) {
                log.info("调价审核通知推送成功 - 订单号: {}, 接收人: {}", orderPO.getOrderNo(), toUser);
            } else {
                log.error("调价审核通知推送失败 - 订单号: {}, 接收人: {}, 错误码: {}, 错误信息: {}",
                        orderPO.getOrderNo(), toUser,
                        response != null ? response.getErrcode() : "null",
                        response != null ? response.getErrmsg() : "null");
            }
        } catch (Exception e) {
            log.error("调价审核通知推送失败 - 订单号: {}, 接收人: {}, 错误: {}", orderPO.getOrderNo(), toUser, e.getMessage(), e);
        }
    }

    // 客户修改提货方式 || 支付成功通知
    public void sendDeliveryMsg(OrderPO order, String title) {
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        String page = configContentVO.getBrand().getPageBaseUrl() + ORDER_DETAIL_PATH + order.getOrderNo();
        DecimalFormat usFormat = new DecimalFormat(PRICE_FORMAT);
        StringBuilder message = new StringBuilder();
        message.append(CUSTOMER_NAME_LABEL).append(order.getCustomerName()).append("\n");
        message.append(ORDER_NO_LABEL).append(order.getOrderNo()).append("\n");
        message.append(PAY_TIME_LABEL).append(DateUtils.localDateTimeToString(order.getPayTime().toLocalDateTime())).append("\n");
        message.append(PAY_AMOUNT_LABEL).append(usFormat.format(order.getPayAmount()));

        try {
            WxCpMessage wxCpMessage = new WxCpMessage();
            wxCpMessage.setAgentId(Integer.parseInt(configContentVO.getBrand().getAgentId()));
            wxCpMessage.setToUser(order.getCaWwid());
            wxCpMessage.setMsgType(TEXT_CARD);
            wxCpMessage.setTitle(title); // 例如"客户修改了提货方式，等待发货"
            wxCpMessage.setDescription(message.toString());
            wxCpMessage.setUrl(page);
            wxCpMessage.setBtnTxt(BTN_DETAIL);

            MessageSendResponse response = weComAdapterClient.messageSend(configContentVO.getBrand().getAgentId(), wxCpMessage);
            if (response != null && response.getErrcode() == 0) {
                log.info("提货方式变更通知推送成功 - 订单号: {}, 接收人: {}", order.getOrderNo(), order.getCaWwid());
            } else {
                log.error("提货方式变更通知推送失败 - 订单号: {}, 接收人: {}, 错误码: {}, 错误信息: {}",
                        order.getOrderNo(), order.getCaWwid(),
                        response != null ? response.getErrcode() : "null",
                        response != null ? response.getErrmsg() : "null");
            }
        } catch (Exception e) {
            log.error("提货方式变更通知推送失败 - 订单号: {}, 接收人: {}, 错误: {}", order.getOrderNo(), order.getCaWwid(), e.getMessage(), e);
        }
    }

    public void sendRefundRequestMsg(OrderPO order, BigDecimal refundAmount) {
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        ConfigContentVO.Refund refund = configContentVO.getRefund();
        if (StringUtils.isEmpty(refund.getRefundApproverWwid())) {
            return;
        }
        DecimalFormat usFormat = new DecimalFormat(PRICE_FORMAT);
        StringBuilder message = new StringBuilder();
        message.append("您有一笔待审批的退款申请，请前往管理后台审批").append("\n");
        message.append(CUSTOMER_NAME_LABEL).append(order.getCustomerName()).append("\n");
        message.append(ORDER_NO_LABEL).append(order.getOrderNo()).append("\n");
        message.append(PAY_TIME_LABEL).append(DateUtils.localDateTimeToString(order.getPayTime().toLocalDateTime())).append("\n");
        message.append(REFUND_AMOUNT_LABEL).append(usFormat.format(refundAmount));

        try {
            WxCpMessage wxCpMessage = new WxCpMessage();
            wxCpMessage.setAgentId(Integer.parseInt(configContentVO.getBrand().getAgentId()));
            wxCpMessage.setToUser(refund.getRefundApproverWwid());
            wxCpMessage.setMsgType(TEXT);
            wxCpMessage.setContent(message.toString());

            MessageSendResponse response = weComAdapterClient.messageSend(configContentVO.getBrand().getAgentId(), wxCpMessage);
            if (response != null && response.getErrcode() == 0) {
                log.info("退款申请通知推送成功 - 订单号: {}, 接收人: {}", order.getOrderNo(), order.getCaWwid());
            } else {
                log.error("退款申请通知推送失败 - 订单号: {}, 接收人: {}, 错误码: {}, 错误信息: {}",
                        order.getOrderNo(), order.getCaWwid(),
                        response != null ? response.getErrcode() : "null",
                        response != null ? response.getErrmsg() : "null");
            }
        } catch (Exception e) {
            log.error("退款申请通知推送失败 - 订单号: {}, 接收人: {}, 错误: {}", order.getOrderNo(), order.getCaWwid(), e.getMessage(), e);
        }
    }

    /**
     * 客户已确认收货通知，仅通知下单CA，跳转订单详情。
     */
    public void sendCustomerConfirmedReceiptMsg(OrderPO order) {
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        String page = configContentVO.getBrand().getPageBaseUrl() + ORDER_DETAIL_PATH + order.getOrderNo();
        DecimalFormat usFormat = new DecimalFormat(PRICE_FORMAT);
        StringBuilder message = new StringBuilder();
        message.append(CUSTOMER_NAME_LABEL).append(order.getCustomerName()).append("\n");
        message.append(ORDER_NO_LABEL).append(order.getOrderNo()).append("\n");
        message.append(PAY_TIME_LABEL).append(DateUtils.localDateTimeToString(order.getPayTime().toLocalDateTime())).append("\n");
        message.append(PAY_AMOUNT_LABEL).append(usFormat.format(order.getPayAmount()));

        try {
            WxCpMessage wxCpMessage = new WxCpMessage();
            wxCpMessage.setAgentId(Integer.parseInt(configContentVO.getBrand().getAgentId()));
            wxCpMessage.setToUser(order.getCaWwid());
            wxCpMessage.setMsgType(TEXT_CARD);
            wxCpMessage.setTitle("客人已确认收货");
            wxCpMessage.setDescription(message.toString());
            wxCpMessage.setUrl(page);
            wxCpMessage.setBtnTxt(BTN_DETAIL);

            MessageSendResponse response = weComAdapterClient.messageSend(configContentVO.getBrand().getAgentId(), wxCpMessage);
            if (response != null && response.getErrcode() == 0) {
                log.info("客户确认收货通知推送成功 - 订单号: {}, 接收人: {}", order.getOrderNo(), order.getCaWwid());
            } else {
                log.error("客户确认收货通知推送失败 - 订单号: {}, 接收人: {}, 错误码: {}, 错误信息: {}",
                        order.getOrderNo(), order.getCaWwid(),
                        response != null ? response.getErrcode() : "null",
                        response != null ? response.getErrmsg() : "null");
            }
        } catch (Exception e) {
            log.error("客户确认收货通知推送失败 - 订单号: {}, 接收人: {}, 错误: {}", order.getOrderNo(), order.getCaWwid(), e.getMessage(), e);
        }
    }

    /**
     * 客户还未确认收货通知，仅通知下单CA，跳转订单详情。
     */
    public void sendCustomerNotConfirmedReceiptMsg(OrderPO order) {
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        String page = configContentVO.getBrand().getPageBaseUrl() + ORDER_DETAIL_PATH + order.getOrderNo();
        DecimalFormat usFormat = new DecimalFormat(PRICE_FORMAT);
        StringBuilder message = new StringBuilder();
        message.append(CUSTOMER_NAME_LABEL).append(order.getCustomerName()).append("\n");
        message.append(ORDER_NO_LABEL).append(order.getOrderNo()).append("\n");
        message.append(PAY_TIME_LABEL).append(DateUtils.localDateTimeToString(order.getPayTime().toLocalDateTime())).append("\n");
        message.append(PAY_AMOUNT_LABEL).append(usFormat.format(order.getPayAmount()));

        try {
            WxCpMessage wxCpMessage = new WxCpMessage();
            wxCpMessage.setAgentId(Integer.parseInt(configContentVO.getBrand().getAgentId()));
            wxCpMessage.setToUser(order.getCaWwid());
            wxCpMessage.setMsgType(TEXT_CARD);
            wxCpMessage.setTitle("客人还未点击确认收货，请提醒客人");
            wxCpMessage.setDescription(message.toString());
            wxCpMessage.setUrl(page);
            wxCpMessage.setBtnTxt(BTN_DETAIL);

            MessageSendResponse response = weComAdapterClient.messageSend(configContentVO.getBrand().getAgentId(), wxCpMessage);
            if (response != null && response.getErrcode() == 0) {
                log.info("客户未确认收货通知推送成功 - 订单号: {}, 接收人: {}", order.getOrderNo(), order.getCaWwid());
            } else {
                log.error("客户未确认收货通知推送失败 - 订单号: {}, 接收人: {}, 错误码: {}, 错误信息: {}",
                        order.getOrderNo(), order.getCaWwid(),
                        response != null ? response.getErrcode() : "null",
                        response != null ? response.getErrmsg() : "null");
            }
        } catch (Exception e) {
            log.error("客户未确认收货通知推送失败 - 订单号: {}, 接收人: {}, 错误: {}", order.getOrderNo(), order.getCaWwid(), e.getMessage(), e);
        }
    }

    public void sendPriceAdjustmentResultMsg(
            OrderPO order,
            OrderItemPO orderItem,
            OrderAdjustPricePO adjustPrice,
            String auditor,
            LocalDateTime auditTime,
            boolean approved,
            String rejectReason
    ) {
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        String page = configContentVO.getBrand().getPageBaseUrl() + ADJUST_PRICE_PATH.formatted(order.getOrderNo(), orderItem.getId());
        DecimalFormat usFormat = new DecimalFormat(PRICE_FORMAT);
        StringBuilder message = new StringBuilder();

        String title = approved ? "您的调价申请已批准" : "您的调价申请已驳回";
        message.append(PRODUCT_NAME_LABEL).append(orderItem.getProductName()).append("\n");
        message.append(ORIGINAL_PRICE_LABEL).append(usFormat.format(adjustPrice.getOriginalPrice())).append("\n");
        message.append(ADJUST_PRICE_LABEL).append(usFormat.format(adjustPrice.getPrice())).append("\n");
        message.append("申请人: ").append(order.getCaName()).append("\n");
        message.append("申请时间: ").append(DateUtils.localDateTimeToString(adjustPrice.getCreatedTime().toLocalDateTime())).append("\n");
        message.append("审核人: ").append(auditor).append("\n");
        message.append("审核时间: ").append(DateUtils.localDateTimeToString(auditTime)).append("\n");
        if (!approved && rejectReason != null) {
            message.append("驳回原因: ").append(rejectReason).append("\n");
        }

        try {
            WxCpMessage wxCpMessage = new WxCpMessage();
            wxCpMessage.setAgentId(Integer.parseInt(configContentVO.getBrand().getAgentId()));
            wxCpMessage.setToUser(order.getCaWwid());
            wxCpMessage.setMsgType(TEXT_CARD);
            wxCpMessage.setTitle(title);
            wxCpMessage.setDescription(message.toString());
            wxCpMessage.setUrl(page);
            wxCpMessage.setBtnTxt(BTN_DETAIL);

            MessageSendResponse response = weComAdapterClient.messageSend(configContentVO.getBrand().getAgentId(), wxCpMessage);
            if (response != null && response.getErrcode() == 0) {
                log.info("调价结果通知推送成功 - 订单号: {}, 接收人: {}", order.getOrderNo(), order.getCaWwid());
            } else {
                log.error("调价结果通知推送失败 - 订单号: {}, 接收人: {}, 错误码: {}, 错误信息: {}",
                        order.getOrderNo(), order.getCaWwid(),
                        response != null ? response.getErrcode() : "null",
                        response != null ? response.getErrmsg() : "null");
            }
        } catch (Exception e) {
            log.error("调价结果通知推送失败 - 订单号: {}, 接收人: {}, 错误: {}", order.getOrderNo(), order.getCaWwid(), e.getMessage(), e);
        }
    }


}
