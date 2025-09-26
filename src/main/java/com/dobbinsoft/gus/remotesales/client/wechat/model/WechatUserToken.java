package com.dobbinsoft.gus.remotesales.client.wechat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WechatUserToken {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private Integer expiresIn;
    @JsonProperty("refresh_token")
    private String refreshToken;

    private String openid;

    private String scope;
    @JsonProperty("is_snapshotuser")
    private Integer isSnapshotuser;

    private String unionid;
}
