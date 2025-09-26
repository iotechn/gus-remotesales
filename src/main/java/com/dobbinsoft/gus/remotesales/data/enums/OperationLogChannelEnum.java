package com.dobbinsoft.gus.remotesales.data.enums;

/**
 * 登录接口类型
 */
public enum OperationLogChannelEnum implements BaseEnums<Integer> {
    WECOM(1, "WECOM"),
    WEB(2, "WEB"),
    WECHAT(3, "WECHAT"),
    ;


    private final Integer code;

    private final String msg;

    OperationLogChannelEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }


    @Override
    public String getMsg() {
        return msg;
    }

}
