package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayStatusEnum implements BaseEnums<Integer> {
    UNPAID(0, "未支付"),
    PAID(1, "已支付");

    private final Integer code;
    private final String msg;
} 