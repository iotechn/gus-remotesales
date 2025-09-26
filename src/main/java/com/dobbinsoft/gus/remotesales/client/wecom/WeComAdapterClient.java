package com.dobbinsoft.gus.remotesales.client.wecom;


import com.dobbinsoft.gus.remotesales.client.wecom.vo.*;

public interface WeComAdapterClient {

    AccessTokenResponse getAccessToken(String agentId, String tenantId);

    WeComAuthTicket getAuthTicket(String agentId, String code);

    WeComUserDetail getUserDetail(String agentId, String ticket);

    WeComUserInfo getUserInfo(String agentId, String userId);

    MessageSendResponse messageSend(String agentId, WxCpMessage message);

    WeComExternalContactResponse getExternalContact(String agentId, String externalUserId);

    JSAPITicket getJSAPITicket(String agentId);

    JSAPITicket getWechatJSAPITicket(String agentId);

    /**
     * 通过邮箱获取userid
     *
     * @param agentId 应用ID
     * @param email 邮箱地址
     * @return 用户ID响应
     */
    WeComUserIdResponse getUserIdByEmail(String agentId, String email);

    WeComDepartmentUserListResponse getDepartmentUser(String agentId, String departmentId);
}
