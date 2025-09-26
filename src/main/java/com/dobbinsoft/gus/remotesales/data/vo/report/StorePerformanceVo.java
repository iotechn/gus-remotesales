package com.dobbinsoft.gus.remotesales.data.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
public class StorePerformanceVo {
    @Schema(description = "销售额")
    BigDecimal salesAmount=new BigDecimal("0.0");
    @Schema(description = "最后销售时间")
    ZonedDateTime lastSalesTime;
}
