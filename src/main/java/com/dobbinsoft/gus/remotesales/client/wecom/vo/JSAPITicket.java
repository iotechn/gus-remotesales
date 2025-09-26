package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class JSAPITicket extends BaseWeComResponse {
    private String ticket;
    @JsonProperty("expires_in")
    private int expiresIn;
}
