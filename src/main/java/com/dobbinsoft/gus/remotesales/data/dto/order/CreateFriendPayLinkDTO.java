package com.dobbinsoft.gus.remotesales.data.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class CreateFriendPayLinkDTO {

    @Schema(description = "order number",requiredMode = Schema.RequiredMode.REQUIRED,example = "order111")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotBlank(message = "请选择支付供应商")
    @Schema(description = "支付供应商ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentProviderId;

    @Schema(description = "pay type full全款 separate分笔")
    private String payType;
}
