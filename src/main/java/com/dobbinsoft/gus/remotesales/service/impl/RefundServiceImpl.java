package com.dobbinsoft.gus.remotesales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dobbinsoft.gus.remotesales.client.gus.payment.TransactionFeignClient;
import com.dobbinsoft.gus.remotesales.client.gus.payment.model.TransactionRefundDTO;
import com.dobbinsoft.gus.remotesales.client.gus.payment.model.TransactionRefundVO;
import com.dobbinsoft.gus.remotesales.data.dto.refund.RefundApplyDTO;
import com.dobbinsoft.gus.remotesales.data.dto.refund.RefundAuditDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.BoSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.enums.*;
import com.dobbinsoft.gus.remotesales.data.po.OrderLogPO;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import com.dobbinsoft.gus.remotesales.data.po.OrderRefundPO;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.mapper.OrderLogMapper;
import com.dobbinsoft.gus.remotesales.mapper.OrderMapper;
import com.dobbinsoft.gus.remotesales.mapper.OrderRefundMapper;
import com.dobbinsoft.gus.remotesales.service.RefundService;
import com.dobbinsoft.gus.remotesales.service.biz.NotificationBizService;
import com.dobbinsoft.gus.remotesales.utils.OrderUtils;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import com.dobbinsoft.gus.web.exception.BasicErrorCode;
import com.dobbinsoft.gus.web.exception.ServiceException;
import com.dobbinsoft.gus.web.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class RefundServiceImpl implements RefundService {

    // Mappers
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderRefundMapper orderRefundMapper;
    @Autowired
    private OrderLogMapper orderLogMapper;
    // BizServices
    @Autowired
    private NotificationBizService notificationBizService;
    // Clients
    @Autowired
    private TransactionFeignClient transactionFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(RefundApplyDTO refundApplyDTO) {
        // 1. Get session info
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        WecomSessionInfoDTO.Store currentStore = wecomSession.getCurrentStore();
        if (!Boolean.TRUE.equals(currentStore.getIsManager())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_ONLY_SM_CAN_APPLY_REFUND);
        }

        // 2. Get and validate order
        // 查询订单
        OrderPO order = orderMapper.selectById(refundApplyDTO.getOrderId());
        if (order == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }
        if (!currentStore.getStoreId().equals(order.getStoreId())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_ONLY_SM_CAN_APPLY_REFUND);
        }


        // 3. Check if order is in refundable status
        if (!OrderStatusEnum.isRefundable(order.getStatus())) {
            throw new ServiceException(RemotesalesErrorCode.STATUS_NO_REFUND);
        }

        // 4. Check existing refunds
        List<OrderRefundPO> pendingRefunds = orderRefundMapper.selectList(
                new LambdaQueryWrapper<OrderRefundPO>()
                        .eq(OrderRefundPO::getOrderNo, order.getOrderNo())
                        .eq(OrderRefundPO::getApproveStatus, OrderRefundApproveStatusEnum.PENDING.getCode())
        );

        List<OrderRefundPO> processingRefunds = orderRefundMapper.selectList(
                new LambdaQueryWrapper<OrderRefundPO>()
                        .eq(OrderRefundPO::getOrderNo, order.getOrderNo())
                        .eq(OrderRefundPO::getApproveStatus, OrderRefundApproveStatusEnum.APPROVED.getCode())
                        .eq(OrderRefundPO::getRefundStatus, OrderRefundStatusEnum.NOT_REFUNDED.getCode())
        );

        if (pendingRefunds.size() + processingRefunds.size() > 0) {
            throw new ServiceException(RemotesalesErrorCode.REFUNDING_ERROR);
        }

        // 5. Calculate total refund amount
        BigDecimal orderAmount = order.getPayAmount();
        BigDecimal totalRefunded = order.getTotalRefund() != null ? order.getTotalRefund() : BigDecimal.ZERO;

        if (refundApplyDTO.getAmount().add(totalRefunded).compareTo(orderAmount) > 0) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_REFUND_AMOUNT_NO);
        }

        // 6. Create refund record
        OrderRefundPO refund = new OrderRefundPO();
        refund.setOrderId(order.getId());
        refund.setOrderNo(order.getOrderNo());
        refund.setRefundNo(OrderUtils.generateRefundNo(order));
        refund.setCreatorWwid(wecomSession.getUserId());
        refund.setCreatorName(wecomSession.getName());
        refund.setRefundAmount(refundApplyDTO.getAmount());
        refund.setRefundComment(refundApplyDTO.getComment());
        refund.setRefundCreateTime(ZonedDateTime.now());
        refund.setRefundStatus(OrderRefundStatusEnum.NOT_REFUNDED.getCode());
        refund.setApproveStatus(OrderRefundApproveStatusEnum.PENDING.getCode());
        refund.setOriginalNo(order.getPayNo());
        refund.setAttachments(refundApplyDTO.getAttachments());

        orderRefundMapper.insert(refund);

        // 7. Update order refund info
        order.setId(order.getId());
        order.setLastRefundNo(refund.getRefundNo());
        orderMapper.updateById(order);

        // 8. Create order log
        OrderLogPO orderLog = OrderLogPO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .statusDesc(BaseEnums.getMsgByCode(order.getStatus(), OrderStatusEnum.class))
                .type(OrderLogTypeEnum.REFUND_APPLY.getCode())
                .typeDesc(OrderLogTypeEnum.REFUND_APPLY.getMsg())
                .refundAmount(refundApplyDTO.getAmount())
                .refundNo(refund.getRefundNo())
                .approveStatus(OrderRefundApproveStatusEnum.PENDING.getCode())
                .approveAction(OrderRefundApproveActionEnum.NO_ACTION.getCode())
                .refundStatus(OrderRefundStatusEnum.NOT_REFUNDED.getCode())
                .description("订单退款申请")
                .comment(refundApplyDTO.getComment())
                .createdByName(wecomSession.getName())
                .build();

        orderLogMapper.insert(orderLog);

        // 9. refund notice
        notificationBizService.sendRefundRequestMsg(order, refundApplyDTO.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(RefundAuditDTO refundAuditDTO) {
        // 1. Get session info
        BoSessionInfoDTO boSession = SessionUtils.getBoSession();

        // 2. Get and validate refund
        OrderRefundPO refund = orderRefundMapper.selectOne(
                new LambdaQueryWrapper<OrderRefundPO>()
                        .eq(OrderRefundPO::getRefundNo, refundAuditDTO.getRefundNo()));
        if (refund == null) {
            throw new ServiceException(RemotesalesErrorCode.REFUND_NULL);
        }

        // 3. Get order info
        OrderPO order = orderMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }

        // 4. Update refund info
        refund.setId(refund.getId());
        refund.setApproveId(boSession.getUserId());
        refund.setApproveName(boSession.getName());
        refund.setApproveComment(refundAuditDTO.getComment());
        refund.setApproveTime(ZonedDateTime.now());

        if (refundAuditDTO.getAction() == OrderRefundApproveActionEnum.APPROVE.getCode().intValue()) {
            // Handle approval
            refund.setApproveStatus(OrderRefundApproveStatusEnum.APPROVED.getCode());
            refund.setRefundStatus(OrderRefundStatusEnum.SUCCESS.getCode());
            refund.setRefundTime(ZonedDateTime.now());


            // Create approval log
            OrderLogPO approvalLog = OrderLogPO.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .status(order.getStatus())
                    .statusDesc(BaseEnums.getMsgByCode(order.getStatus(), OrderStatusEnum.class))
                    .type(OrderLogTypeEnum.REFUND_APPROVED.getCode())
                    .typeDesc(OrderLogTypeEnum.REFUND_APPROVED.getMsg())
                    .refundAmount(refund.getRefundAmount())
                    .refundNo(refund.getRefundNo())
                    .approveStatus(OrderRefundApproveStatusEnum.APPROVED.getCode())
                    .approveAction(OrderRefundApproveActionEnum.APPROVE.getCode())
                    .refundStatus(OrderRefundStatusEnum.NOT_REFUNDED.getCode())
                    .description("订单退款通过")
                    .comment(refundAuditDTO.getComment())
                    .createdByName(boSession.getName())
                    .build();
            orderLogMapper.insert(approvalLog);

            // Call refund service
            processRefund(order, refund);
            orderRefundMapper.updateById(refund);
            //退款成功后更新order 次数和总退款金额
            order.setTotalRefund(Objects.nonNull(order.getTotalRefund())?order.getTotalRefund().add(refund.getRefundAmount()):refund.getRefundAmount());
            order.setTotalRefundCount((order.getTotalRefundCount() != null ? order.getTotalRefundCount() : 0) + 1);
            orderMapper.updateById(order);
            try {
                // Send notification, 不影响主事务，所以try catch
                List<String> notifyUsers = new ArrayList<>();
                notifyUsers.add(refund.getCreatorWwid());
                notifyUsers.add(order.getCaWwid());
                notificationBizService.sendRefundMsg(order, "退款申请通过", refund.getRefundAmount(), "", notifyUsers);

            } catch (Exception e) {
                log.error("Refund process failed", e);
            }

        } else if (refundAuditDTO.getAction() == OrderRefundApproveActionEnum.REJECT.getCode().intValue()) {
            // Handle rejection
            refund.setApproveStatus(OrderRefundApproveStatusEnum.REJECTED.getCode());
            refund.setRefundStatus(OrderRefundStatusEnum.FAILED.getCode());
            orderRefundMapper.updateById(refund);

            // Create rejection log
            OrderLogPO rejectionLog = OrderLogPO.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .status(order.getStatus())
                    .statusDesc(BaseEnums.getMsgByCode(order.getStatus(), OrderStatusEnum.class))
                    .type(OrderLogTypeEnum.REFUND_REJECTED.getCode())
                    .typeDesc(OrderLogTypeEnum.REFUND_REJECTED.getMsg())
                    .refundAmount(refund.getRefundAmount())
                    .refundNo(refund.getRefundNo())
                    .approveStatus(OrderRefundApproveStatusEnum.REJECTED.getCode())
                    .approveAction(OrderRefundApproveActionEnum.REJECT.getCode())
                    .refundStatus(OrderRefundStatusEnum.NOT_REFUNDED.getCode())
                    .description("订单退款驳回")
                    .comment(refundAuditDTO.getComment())
                    .createdByName(boSession.getName())
                    .build();
            orderLogMapper.insert(rejectionLog);

            // Send notification
            List<String> notifyUsers = new ArrayList<>();
            notifyUsers.add(refund.getCreatorWwid());
            notifyUsers.add(order.getCaWwid());
            String rejectReason = StringUtils.hasText(refundAuditDTO.getComment()) ?
                    "驳回理由: " + refundAuditDTO.getComment() : "驳回理由: 无";
            notificationBizService.sendRefundMsg(order, "退款申请驳回", refund.getRefundAmount(), rejectReason, notifyUsers);
        }
    }

    private void processRefund(OrderPO order, OrderRefundPO refund) {
        TransactionRefundDTO transactionRefundDTO = new TransactionRefundDTO();

        transactionRefundDTO.setProviderId(order.getPaymentProviderId());
        transactionRefundDTO.setRefundNo(refund.getRefundNo());
        transactionRefundDTO.setRefundAmount(refund.getRefundAmount());
        transactionRefundDTO.setReason(refund.getRefundComment());
        transactionRefundDTO.setOrderNo(order.getOrderNo());
        R<TransactionRefundVO> r = transactionFeignClient.refund(transactionRefundDTO);
        if (!BasicErrorCode.SUCCESS.getCode().equals(r.getCode())) {
            throw new ServiceException(r.getCode(), r.getMessage());
        }
        TransactionRefundVO data = r.getData();
        refund.setQueryId(data.getRefundId());
    }

}

