package com.dobbinsoft.gus.remotesales.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderLogTypeEnum implements BaseEnums<Integer> {
    CREATE_ORDER(0, "销售创建订单"),
    APPLY_ADJUST(1, "店长调价审批中"),
    APPLY_ADJUST_APPROVE(2, "店长调价审批通过"),
    APPLY_ADJUST_REJECT(3, "店长调价审批驳回"),
    SHARE_ORDER(10, "销售分享订单"),
    PAY_SUCCESS(20, "客人支付订单"),
    MODIFY_DELIVERY_METHOD(30, "客人选择配送方式"),
    RESET_DELIVERY_METHOD(31, "销售重置物流"),
    UPLOAD_POST_NO(32, "销售输入POS单号"),
    UPLOAD_POST_NO_UPDATE(33, "销售修改POS单号/上传小票"),
    PICKUP_SCAN(40, "销售扫码核销"),
    SALES_DELIVERY(50, "销售物流发货"),
    MODIFY_LOGISTICS(55, "修改物流信息"),
    CONFIRM_RECEIPT(60, "客人确认收货（客人）"),
    APPLY_INVOICE(65, "客人申请发票"),
    ORDER_EXPIRED(70, "订单未支付自动过期"),
    REFUND_APPLY(80, "订单退款申请"),
    REFUND_APPROVED(81, "订单退款审核通过"),
    REFUND_REJECTED(82, "订单退款审核驳回"),
    REFUND_PROCESSING(83, "系统退款中"),
    REFUND_FAILED(84, "订单退款失败"),
    REFUND_SUCCESS(85, "订单退款成功"),
    AUTO_RECEIPT(86, "客人确认收货（系统）"),
    UNKNOWN(-1, "未知");

    private final Integer code;
    private final String msg;

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
} 