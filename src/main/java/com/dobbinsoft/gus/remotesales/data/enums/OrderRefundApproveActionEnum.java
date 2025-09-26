package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderRefundApproveActionEnum implements BaseEnums<Integer> {
    NO_ACTION(0, "无操作"),
    REJECT(1, "驳回"),
    APPROVE(2, "通过"),
    ;

    private final Integer code;
    private final String msg;
} 