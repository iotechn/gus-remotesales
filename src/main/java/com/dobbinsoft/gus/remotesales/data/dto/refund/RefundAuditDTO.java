package com.dobbinsoft.gus.remotesales.data.dto.refund;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundAuditDTO {

    @NotBlank
    @Schema(description = "退款单号")
    private String refundNo;

    @Schema(description = "驳回理由")
    private String comment;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderRefundApproveActionEnum
     */
    @NotNull
    @Schema(description = "审批操作（参照枚举 ApproveAction，0=无操作, 1=驳回, 2=通过）")
    private Integer action;

}
