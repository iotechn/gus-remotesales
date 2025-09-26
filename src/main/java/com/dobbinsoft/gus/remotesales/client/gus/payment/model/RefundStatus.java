package com.dobbinsoft.gus.remotesales.client.gus.payment.model;

/**
 * 退款状态枚举
 * 独立于实体类，用于DTO和VO中
 */
public enum RefundStatus {
    PROCESSING,  // 退款处理中
    SUCCESS,     // 退款成功
    FAILED,      // 退款失败
    CLOSED       // 退款关闭
}
