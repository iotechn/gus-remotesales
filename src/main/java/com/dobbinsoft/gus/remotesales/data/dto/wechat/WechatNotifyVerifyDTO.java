package com.dobbinsoft.gus.remotesales.data.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
public class WechatNotifyVerifyDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3980222955979552447L;

    @JsonProperty("msg_signature")
    private String msgSignature;

    private String timestamp;

    private String nonce;

    private String echostr;

}
