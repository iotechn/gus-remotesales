package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum implements BaseEnums<Integer> {
    //UNKNOWN(-1, "未知"),
    TO_FORWARD(0, "待分享"),
    TO_PAY(10, "待支付"),
    RESETTING_DELIVER_METHOD(20, "重置物流中"),
    TO_DELIVER(30, "待发货"),
    TO_PICKUP(40, "待提货"), // 待发货/提货 可以重置物流
    TO_RECEIVE(50, "待收货"),
    TO_BE_COMPLETED(80, "待完成"),
    COMPLETED(60, "已完成"),
    EXPIRED(70, "已取消");


    private final Integer code;
    private final String msg;

    /**
     *
     * @return 判断是否能够重置物流
     */
    public boolean resetDeliverAble() {
        return this == TO_DELIVER || this == TO_PICKUP;
    }

    /**
     * 判断订单状态是否允许退款
     * @param status 订单状态码
     * @return 是否允许退款
     */
    public static boolean isRefundable(Integer status) {
        if (status == null) {
            return false;
        }
        return status.equals(RESETTING_DELIVER_METHOD.getCode()) ||
               status.equals(TO_DELIVER.getCode()) ||
               status.equals(TO_PICKUP.getCode()) ||
               status.equals(TO_RECEIVE.getCode()) ||
               status.equals(TO_BE_COMPLETED.getCode()) ||
               status.equals(COMPLETED.getCode());
    }
} 