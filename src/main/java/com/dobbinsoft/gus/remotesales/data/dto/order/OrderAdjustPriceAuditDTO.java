package com.dobbinsoft.gus.remotesales.data.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderAdjustPriceAuditDTO {

    @NotNull(message = "order adjust price id can't be null")
    @Schema(description = "订单调价ID")
    private Long orderAdjustPriceId;

    @NotNull(message = "status can't be null")
    @Schema(description = "审核状态 1=通过 2=驳回")
    private Integer status;

    @Schema(description = "拒绝备注")
    private String rejectRemark;

}
