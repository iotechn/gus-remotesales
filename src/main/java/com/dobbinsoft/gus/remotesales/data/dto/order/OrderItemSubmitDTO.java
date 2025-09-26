package com.dobbinsoft.gus.remotesales.data.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemSubmitDTO {

    @NotBlank(message = "sku can't be empty")
    @Schema(description = "SKU 编码")
    private String sku;

    @NotNull(message = "qty can't be null")
    @Schema(description = "购买 数量")
    private Integer qty;

    @Schema(description = "商品 备注")
    private String remark;

    @Schema(description = "商品 内部备注")
    private String innerRemark;

    @Schema(description = "调价")
    private BigDecimal adjustPrice;

    @Schema(description = "调价备注")
    private String adjustPriceInnerRemark;


}
