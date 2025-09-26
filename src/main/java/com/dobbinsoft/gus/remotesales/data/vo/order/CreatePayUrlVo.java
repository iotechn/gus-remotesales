package com.dobbinsoft.gus.remotesales.data.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreatePayUrlVo {

    @Schema(description = "order info")
    private OrderVO order;

    @Schema(description = "prepay", example = "https://www.baidu.com")
    private Object prepay;

}
