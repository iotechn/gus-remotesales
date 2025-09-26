package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderAdjustPriceStatusEnum implements BaseEnums<Integer> {
    PENDING(0, "审核中"),
    APPROVED(1, "通过"),
    REJECTED(2, "驳回"),
    ;

    private final Integer code;
    private final String msg;
} 