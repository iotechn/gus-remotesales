package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderTypeEnum implements BaseEnums<Integer> {
    ORDER(1, "远程销售单"),
    DEPOSIT_ORDER(2, "订金单");

    private final Integer code;
    private final String msg;
} 