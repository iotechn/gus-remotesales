package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageSendResponse extends BaseWeComResponse {
    /**
     * 无效的userId
     */
    private String invalidUser;
    /**
     * 无效的部门ID
     */
    private String invalidParty;
    /**
     * 无效的tagID
     */
    private String invalidTag;
}
