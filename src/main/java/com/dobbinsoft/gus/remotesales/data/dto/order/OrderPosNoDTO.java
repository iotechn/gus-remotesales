package com.dobbinsoft.gus.remotesales.data.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPosNoDTO {
    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "POS小票号")
    private String posNumber;

    @Schema(description = "小票图片（URL）")
    private String receipt;

    @Schema(description = "小票单号")
    private String receiptNumber;

}
