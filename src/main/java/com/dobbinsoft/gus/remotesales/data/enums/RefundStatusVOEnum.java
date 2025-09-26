package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefundStatusVOEnum implements BaseEnums<Integer> {
    ALL_REFUND(0, "全部退款"),
    PARTIAL_REFUND(1,"部分退款"),
    NOT_REFUND(2,"无退款"),
    ;

    private final Integer code;
    private final String msg;


} 