package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderRefundApproveStatusEnum implements BaseEnums<Integer> {
    PENDING(0, "退款中"),
    REJECTED(1, "已驳回"),
    APPROVED(2, "已退款"),
    ;

    private final Integer code;
    private final String msg;
} 