package com.dobbinsoft.gus.remotesales.data.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class PaymentVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "支付类型")
    private String payType;
    @Schema(description = "付款金额")
    private BigDecimal payAmount;
    @Schema(description = "支付日期")
    private ZonedDateTime payDate;
    @Schema(description = "支付方式")
    private String payMethod;
    @Schema(description = "流水号")
    private String paySeqNo;
}
