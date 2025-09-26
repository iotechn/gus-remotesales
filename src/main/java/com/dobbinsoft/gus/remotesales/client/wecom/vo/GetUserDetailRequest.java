package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 获取用户敏感信息请求体
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetUserDetailRequest {

    @JsonProperty("user_ticket")
    private String userTicket;
}
