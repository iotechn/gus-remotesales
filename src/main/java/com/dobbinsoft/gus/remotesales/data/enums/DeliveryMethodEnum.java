package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryMethodEnum implements BaseEnums<Integer> {
    LOGISTICS(0, "物流发货"),
    SELF_PICKUP(1, "客户自提");

    private final Integer code;
    private final String msg;
} 