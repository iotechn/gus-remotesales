package com.dobbinsoft.gus.remotesales.data.dto.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WechatSessionInfoDTO {

    private String openid;

    private String unionid;

    private String headImgUrl;

    private String nickname;
    @JsonIgnore
    private Integer isSnapshotuser;
}
