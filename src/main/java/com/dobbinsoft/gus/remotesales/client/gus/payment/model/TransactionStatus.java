package com.dobbinsoft.gus.remotesales.client.gus.payment.model;

/**
 * 交易状态枚举
 * 独立于实体类，用于DTO和VO中
 */
public enum TransactionStatus {
    UNPAY,
    SUCCESS,
    REFUND,
    CLOSED
}
