package com.dobbinsoft.gus.remotesales.data.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderAdjustPriceApplyDTO {

    @NotNull(message = "order item id can't be null")
    @Schema(description = "订单Item ID")
    private Long orderItemId;

    @NotNull(message = "status can't be null")
    @Schema(description = "审核状态 1=通过 2=驳回")
    private BigDecimal price;

    @Schema(description = "内部备注")
    private String innerRemark;

}
