package com.dobbinsoft.gus.remotesales.client.wechat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WeChatUserInfo {

    @JsonProperty("openid")
    private String openId;

    @JsonProperty("unionid")
    private String unionId;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("headimgurl")
    private String headImgUrl;

    @JsonProperty("sex")
    private Integer sex;

    @JsonProperty("province")
    private String province;

    @JsonProperty("city")
    private String city;

    @JsonProperty("country")
    private String country;

    @JsonProperty("language")
    private String language;

    @JsonProperty("privilege")
    private String[] privilege;
}
