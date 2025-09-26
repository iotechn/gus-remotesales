package com.dobbinsoft.gus.remotesales.data.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
public class SalesPerformanceAllVo  extends SalesPerformanceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "最后销售时间")
    private ZonedDateTime lastSalesDate;

}
