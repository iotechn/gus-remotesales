package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayTypeEnum implements BaseEnums<String> {
    SPLIT("SPLIT", "分笔支付"),
    WHOLE("WHOLE", "整笔支付"),
    ;

    private final String code;

    private final String msg;

}
