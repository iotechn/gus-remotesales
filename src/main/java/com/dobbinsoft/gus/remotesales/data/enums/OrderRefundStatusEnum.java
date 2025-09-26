package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderRefundStatusEnum implements BaseEnums<Integer> {
    NOT_REFUNDED(0, "退款中"),
    SUCCESS(1, "已退款"),
    FAILED(2, "已驳回"),
    ;

    private final Integer code;
    private final String msg;
} 