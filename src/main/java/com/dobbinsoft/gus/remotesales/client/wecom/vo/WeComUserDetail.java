package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WeComUserDetail extends BaseWeComResponse {

    private String userid;

    private String gender;

    private String avatar;
    @JsonProperty("qr_code")
    private String qrCode;

    private String mobile;

    private String email;
    @JsonProperty("biz_mail")
    private String bizMail;

    private String address;
}
