package com.dobbinsoft.gus.remotesales.data.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderExpressDTO {

    @NotNull(message = "order id can't be null")
    @Schema(description = "订单ID")
    private Long orderId;
    @Schema(description = "是否加急 true 加急")
    private Boolean urgent;

}
