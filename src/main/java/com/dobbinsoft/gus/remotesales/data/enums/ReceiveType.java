package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReceiveType implements BaseEnums<Integer> {
    SELF(0, "客人主动"),
    AUTO(1, "自动收货");

    private final Integer code;
    private final String msg;
} 