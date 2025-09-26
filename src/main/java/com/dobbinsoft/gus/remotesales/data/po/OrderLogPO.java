package com.dobbinsoft.gus.remotesales.data.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dobbinsoft.gus.remotesales.data.po.base.SoftDeleteMyBatisBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@TableName("rs_order_log")
@Schema(description = "订单日志表")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLogPO extends SoftDeleteMyBatisBaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderStatusEnum
     */
    @Schema(description = "订单状态（0=待转发, 10=待付款, 20=重置物流中, 30=待发货, 40=待提货, 50=待收货, 60=已完成, 70=已过期, -1=未知）")
    private Integer status;

    @Schema(description = "订单状态描述")
    private String statusDesc;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderLogTypeEnum
     */
    @Schema(description = "订单日志类型（参照枚举 Type：0=提交订单, 10=分享订单, 20=付款成功, 30=修改订单配送方式, 40=自提货物扫码核销, 50=销售发货, 55=修改物流信息, 60=确认收货, 70=订单超过24小时未支付过期, 80=订单退款申请, 81=订单退款审核通过, 82=订单退款审核驳回, 83=系统退款中, 84=订单退款失败, 85=订单退款成功, -1=未知）")
    private Integer type;

    @Schema(description = "订单日志类型描述")
    private String typeDesc;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款编号")
    private String refundNo;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderRefundApproveStatusEnum
     */
    @Schema(description = "审核状态（0=待审核, 1=审核驳回, 2=审核通过）")
    private Integer approveStatus;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderRefundStatusEnum
     */
    @Schema(description = "退款状态（参照枚举 RefundStatus，退款状态（0=退款中, 1=已退款, 2=已驳回）")
    private Integer refundStatus;

    @Schema(description = "日志描述")
    private String description;

    @Schema(description = "备注（UTF-8编码，二进制比较）")
    private String comment;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderRefundApproveActionEnum
     */
    @Schema(description = "审批操作（参照枚举 ApproveAction，0=无操作, 1=驳回, 2=通过）")
    private Integer approveAction;


    @Schema(description = "订单金额")
    private BigDecimal amount;

    @Schema(description = "执行人名字")
    private String createdByName;
}
