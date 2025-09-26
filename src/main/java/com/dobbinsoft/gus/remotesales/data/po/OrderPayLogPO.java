package com.dobbinsoft.gus.remotesales.data.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dobbinsoft.gus.remotesales.data.po.base.SoftDeleteMyBatisBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@TableName("rs_order_pay_log")
@Schema(description = "订单支付记录表")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPayLogPO extends SoftDeleteMyBatisBaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付金额")
    private ZonedDateTime payTime;

    @Schema(description = "收银台商户编号")
    private String tidNo;

    @Schema(description = "支付方式")
    private String payModeSet;

    @Schema(description = "工行订单号")
    private String icbcOrderId;

    @Schema(description = "第三方订单号")
    private String thirdTradeNo;

}
