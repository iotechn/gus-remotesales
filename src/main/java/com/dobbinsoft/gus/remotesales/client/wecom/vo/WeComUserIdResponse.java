package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WeComUserIdResponse extends BaseWeComResponse {
    @JsonProperty("userid")
    private String userid;
} 