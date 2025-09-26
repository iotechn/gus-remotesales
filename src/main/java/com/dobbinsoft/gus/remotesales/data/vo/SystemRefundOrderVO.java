package com.dobbinsoft.gus.remotesales.data.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class SystemRefundOrderVO {
    @Schema(description = "订单号")
    private String orderNo;
    @Schema(description = "订单下单时间")
    private String orderCreateTime;
    @Schema(description = "员工ID")
    private String xstoreId;
    @Schema(description = "小票单号")
    private String receiptNumber;
    @Schema(description = "门店")
    private String storeName;
    @Schema(description = "客户昵称")
    private String customerNickname;
    @Schema(description = "POS单号")
    private String posNumber;
    @Schema(description = "退款方式")
    private String payMethod;
    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "退款单号")
    private String refundNo;

    @Schema(description = "申请人WWID")
    private String creatorWwid;

    @Schema(description = "申请姓名")
    private String creatorName;

    @Schema(description = "申请退款时间")
    private ZonedDateTime refundCreateTime;

    @Schema(description = "申请退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "申请退款备注")
    private String refundComment;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderRefundStatusEnum
     */
    @Schema(description = "退款状态（0=退款中, 1=已退款, 2=已驳回）")
    private Integer refundStatus;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderRefundApproveStatusEnum
     */
    @Schema(description = "审核状态（0=待审核, 1=审核驳回, 2=审核通过）")
    private Integer approveStatus;

    @Schema(description = "当前最新被审核时间")
    private ZonedDateTime approveTime;

    @Schema(description = "当前最新驳回/通过理由备注")
    private String approveComment;

    @Schema(description = "当前最新退款时间")
    private ZonedDateTime refundTime;

    @Schema(description = "支付公司退款流水号")
    private String queryId;

    @Schema(description = "支付公司付款时流水号")
    private String originalNo;

    @Schema(description = "批准者ID")
    private String approveId;

    @Schema(description = "批准者名")
    private String approveName;

    @Schema(description = "退款备注，记录失败原因")
    private String failComment;
    @Schema(description = "退款附件url")
    @TableField("attachments")
    private String attachments;
    @Schema(description = "剩余金额")
    private BigDecimal remainingAmount;



}
