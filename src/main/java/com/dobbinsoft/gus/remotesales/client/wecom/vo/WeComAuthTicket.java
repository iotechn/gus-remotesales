package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WeComAuthTicket extends BaseWeComResponse {

    private String userid;
    @JsonProperty("user_ticket")
    private String userTicket;
}
