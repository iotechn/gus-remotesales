package com.dobbinsoft.gus.remotesales.data.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesPerformanceByKeyWordsVo {
    @Schema(description = "员工名称")
    String keyWord;
    @Schema(description = "今日")
    BigDecimal todayValue;


    @Schema(description = "昨日")
    BigDecimal yesterdayValue;


    @Schema(description = "本周")
    BigDecimal weekValue;


    @Schema(description = "本月")
    BigDecimal monthValue;


}
