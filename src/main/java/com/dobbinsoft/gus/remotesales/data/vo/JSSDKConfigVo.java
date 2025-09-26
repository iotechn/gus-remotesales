package com.dobbinsoft.gus.remotesales.data.vo;

import lombok.Data;

@Data
public class JSSDKConfigVo {

    private String timestamp;

    private Long tstamp;

    private String nonceStr;

    private String signature;

}
