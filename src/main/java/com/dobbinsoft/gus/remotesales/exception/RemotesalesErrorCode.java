package com.dobbinsoft.gus.remotesales.exception;

import com.dobbinsoft.gus.web.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum RemotesalesErrorCode implements ErrorCode {

    SUCCESS("0", "success"),
    UNAUTHORIZED("401", "Unauthorized"),
    NO_PERMISSION("403", "No Permission"),
    NO_RESOURCE("404", "No Resource"),
    PARAMERROR("1000", "params error"),
    SYSTEM_ERROR("100000", "system error"),
    NO_DATA_TO_EXPORT("100001","no data to export" ),
    EXPORT_FAILED("100002","export failed" ),
    STORE_ACCESS_DENIED("100003","STORE access denied" ),
    SUCCESS_IS_SNAPSHOTUSER("100004","success" ),
    // 订单错误码范围 1100 ～ 1299
    ORDER_NOT_EXIST("1100", "order not exists!"),
    ORDER_EXTERNAL_CONTACT_NOT_EXIST("1101", "external contact not exists!"),
    ORDER_NOT_FOR_YOU("1102", "您无权限查看此订单"),
    ORDER_PICKUP_ADDRESS_NOT_EXIST("1103", "提货门店不存在"),
    ORDER_SHARE_ONLY("1104", "只能发送自己创建的订单"),
    ORDER_SHARE_CHECK("1105",  "只有待转发订单才可以转发"),
    ORDER_NOT_PAID("1106", "订单尚未支付"),
    ORDER_POS_NO_NOT_EXIST("1107", "请先为订单设置Pos NO"),
    ORDER_EXPRESS_NO_NOT_EXIST("1108", "下物流单失败"),
    ORDER_DELIVER_METHOD_NOT_LOGISTICS("1109", "并非物流发货订单"),
    ORDER_DELIVER_METHOD_NOT_SELF_PICKUP("1110", "并非自提订单"),
    ORDER_NOT_SHIPPED("1111", "订单还未发货"),
    ORDER_NOT_TO_PICKUP("1112", "订单并非等待提货"),
    ORDER_QR_CODE_EXPIRED("1113", "二维码已经过期"),
    ORDER_CANNOT_RESET_DELIVER("1114", "订单状态不允许重置物流"),
    ORDER_STATUS_CANNOT_CANCEL_EXPRESS("1115", "订单状态不允许取消发货"),
    ORDER_CANCEL_EXPRESS_FAILED("1116", "物流单取消失败"),
    ORDER_PAID_ORDER_CANNOT_ADJUST_PRICE("1117", "已支付订单无法调整价格"),
    ORDER_ADJUST_PRICE_EXIST_PENDING("1118", "该商品存在待审批的调价单，无法重复申请"),
    ORDER_STORE_NOT_EXIST("1119", "您并非门店员工无法下单"),
    ORDER_IS_COMPLETE("1120", "订单已完成"),
    ORDER_EXPRESS_ADDRESS_IS_NULL("1121", "收货信息为空.请补充收货地址"),
    ORDER_ONLY_SM_CAN_APPLY_REFUND("1122", "仅店长可申请退款"),
    QUERY_ORDER_ERROR("1123", "工行订单查询接口请求失败"),
    INSERT_PAY_LOG_ERROR("1124", "工行订单查询新增支付记录失败"),

    // 退款错误码范围 1200 ～ 1299
    REFUND_NO_PERMISSIONS("1200", "权限不足，不可申请退款"),
    STATUS_NO_REFUND("1201", "当前订单状态不可退款"),
    ORDER_REFUND_AMOUNT_NO("1202", "可退款金额不足"),
    REFUND_APPROVAL_PERMISSIONS("1203", "权限不足，不可审核此退款"),
    REFUND_NULL("1204", "退款单不存在"),
    REFUND_EXCEPTION("1205", "退款异常"),
    REFUND_NO_NULL("1206", "退款单有误，请检查"),
    REFUNDING_ERROR("1207", "目前存在审核中或退款中订单，不可申请退款"),


    // 商品错误码范围 1300 ～ 1399
    PRODUCT_NOT_FOUND("1301","商品不存在"),
    PRODUCT_NOT_PRICE("1302","商品价格不存在"),
    PRODUCT_CONNECTION_REFUSED("1303","商品数据获取超时，请稍后再试"),

    // icbc错误码
    ICBC_ERROR("30001","工行接口调用失败"),
    ICBC_CAN_NOT_FIND_CONFIG("30002","未查到工行配置项"),


    CDB_MAIN_NET_ERROR("40001","CDB 网络异常");
    /**
     * error code
     */
    private final String code;
    /**
     * error message
     */
    private final String message;

    RemotesalesErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
