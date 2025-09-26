package com.dobbinsoft.gus.remotesales.data.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SalesPerformanceVo {
    @Schema(description = "销售额")
    BigDecimal salesAmount=new BigDecimal("0.0");
    @Schema(description = "销售量")
    BigDecimal salesCount=new BigDecimal(0);
    @Schema(description = "订单量")
    BigDecimal orderCount=new BigDecimal(0);
}
