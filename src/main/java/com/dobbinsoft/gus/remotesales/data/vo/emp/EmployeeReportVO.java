package com.dobbinsoft.gus.remotesales.data.vo.emp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class EmployeeReportVO {
    @Schema(description = "员工Id")
    String cawwId;
    @Schema(description = "员工名称")
    String caName;
    @Schema(description = "总销售订单量")
    Integer salesOrderCount;
    @Schema(description = "总销售金额")
    BigDecimal salesAmount;
    @Schema(description = "最后销售时间")
    ZonedDateTime lastSalesTime;


}
