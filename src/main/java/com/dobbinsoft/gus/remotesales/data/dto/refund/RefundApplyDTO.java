package com.dobbinsoft.gus.remotesales.data.dto.refund;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundApplyDTO {

    @NotNull(message = "order id can't be null")
    @Schema(description = "订单ID")
    private Long orderId;

    @NotNull(message = "refund amount can't be null")
    @Schema(description = "退款金额")
    private BigDecimal amount;

    @NotNull(message = "Refund reason can't be null")
    @Schema(description = "退款理由")
    private String comment;
    @Schema(description = "退款附件地址url")
    private String attachments;

}
