package com.dobbinsoft.gus.remotesales.client.wecom;

import com.dobbinsoft.gus.common.utils.context.GenericRequestContextHolder;
import com.dobbinsoft.gus.common.utils.context.bo.TenantContext;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.*;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.web.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信服务客户端实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeComAdapterClientImpl implements WeComAdapterClient {

    private static final String WECOM_BASE_URL = "https://qyapi.weixin.qq.com";
    private static final String USER_INFO = "/cgi-bin/user/get?access_token={access_token}&userid={userid}";
    private static final String ACCESS_TOKEN_URL = "/cgi-bin/gettoken?corpid={corp_id}&corpsecret={secret}";
    private static final String GET_AUTH_TICKET_URL = "/cgi-bin/user/getuserinfo?access_token={access_token}&code={code}";
    private static final String GET_USER_DETAIL_URL = "/cgi-bin/auth/getuserdetail?access_token={access_token}";
    private static final String SEND_MESSAGE_URL = "/cgi-bin/message/send?access_token={accessToken}";
    private static final String GET_EXTERNAL_CONTACT_URL = "/cgi-bin/externalcontact/get?access_token={access_token}&external_userid={external_userid}";
    private static final String GET_WECHAT_JSAPI_TICKET_URL = "/cgi-bin/get_jsapi_ticket?access_token={access_token}";
    private static final String GET_AGENT_JSAPI_TICKET_URL = "/cgi-bin/ticket/get?access_token={access_token}&type={type}";
    private static final String GET_USERID_BY_EMAIL_URL = "/cgi-bin/user/get_userid_by_email?access_token={access_token}";
    private static final String GET_DEPARTMENT_USER_LIST_URL = "/cgi-bin/user/list?access_token={access_token}&department_id={department_id}";
    private static final int SUCCESS_CODE = 0;
    private static final String RESPONSE_NULL_ERROR = "Response is null";

    private final RestTemplate restTemplate = new RestTemplate();

    private String getTenantId() {
        return GenericRequestContextHolder.getTenantContext()
                .map(TenantContext::getTenantId)
                .orElseThrow(() -> {
                   log.error("getTenantId failed!!!");
                   return new ServiceException(RemotesalesErrorCode.SYSTEM_ERROR);
                });
    }

    private <T> T handleResponse(ResponseEntity<T> response, String operation, Object request) {
        assert response != null && response.getBody() != null;
        T body = response.getBody();
        // Log
        if (body instanceof BaseWeComResponse baseWeComResponse) {
            if (SUCCESS_CODE == baseWeComResponse.getErrcode()) {
                log.info("{}: success for request:{}", operation, request);
            } else {
                log.warn("{}: failed for request:{}, errcode:{}, errmsg:{}", operation, request, baseWeComResponse.getErrcode(), baseWeComResponse.getErrmsg());
            }
        }
        return body;
    }

    @Override
    public AccessTokenResponse getAccessToken(String corpId, String secret) {
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = WECOM_BASE_URL + ACCESS_TOKEN_URL;
            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, AccessTokenResponse.class, corpId, secret);

            if (response.getBody() == null) {
                log.warn("未获取到accessToken响应 tenantId:{}", secret);
                AccessTokenResponse errorResponse = new AccessTokenResponse();
                errorResponse.setErrcode(500);
                errorResponse.setErrmsg(RESPONSE_NULL_ERROR);
                return errorResponse;
            }

            AccessTokenResponse result = response.getBody();
            if (SUCCESS_CODE == result.getErrcode()) {
                log.info("成功获取accessToken tenantId:{}", secret);
            } else {
                log.warn("未获取到accessToken tenantId:{}, errcode:{}, errmsg:{}", secret, result.getErrcode(), result.getErrmsg());
            }
            return result;
        } catch (Exception e) {
            log.error("getAccessToken: exception:{}, tenantId:{}, agentId:{}", e.getMessage(), secret, corpId);
            AccessTokenResponse errorResponse = new AccessTokenResponse();
            errorResponse.setErrcode(500);
            errorResponse.setErrmsg("Exception: " + e.getMessage());
            log.error("getAccessToken: exception:", e);
            return errorResponse;
        }
    }

    @Override
    public WeComAuthTicket getAuthTicket(String agentId, String code) {
        if (StringUtils.isBlank(code)) {
            log.error("getAuthTicket: code is null or empty");
            WeComAuthTicket errorResponse = new WeComAuthTicket();
            errorResponse.setErrcode(400);
            errorResponse.setErrmsg("Code is null or empty");
            return errorResponse;
        }
        try {
            String tenantId = getTenantId();

            AccessTokenResponse accessToken = this.getAccessToken(agentId, tenantId);
            if (accessToken == null || StringUtils.isBlank(accessToken.getAccessToken()) || StringUtils.isBlank(accessToken.getAccessId())) {
                log.error("getAuthTicket: invalid accessToken for code:{}", code);
                WeComAuthTicket errorResponse = new WeComAuthTicket();
                errorResponse.setErrcode(500);
                errorResponse.setErrmsg("Invalid access token");
                return errorResponse;
            }

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = WECOM_BASE_URL + GET_AUTH_TICKET_URL;
            ResponseEntity<WeComAuthTicket> resultResponse = restTemplate.exchange(
                    url, HttpMethod.GET, entity, WeComAuthTicket.class, accessToken.getAccessToken(), code);

            return handleResponse(resultResponse, "getAuthTicket", code);
        } catch (Exception e) {
            log.error("getAuthTicket: exception:{}, code:{}", e.getMessage(), code);
            log.error("getAuthTicket: exception:", e);
            WeComAuthTicket errorResponse = new WeComAuthTicket();
            errorResponse.setErrcode(500);
            errorResponse.setErrmsg("Exception: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public WeComUserDetail getUserDetail(String agentId, String ticket) {
        if (StringUtils.isBlank(ticket)) {
            log.error("getUserDetail: ticket is null or empty");
            WeComUserDetail errorResponse = new WeComUserDetail();
            errorResponse.setErrcode(400);
            errorResponse.setErrmsg("Ticket is null or empty");
            return errorResponse;
        }
        try {
            String tenantId = getTenantId();

            AccessTokenResponse accessToken = this.getAccessToken(agentId, tenantId);
            if (accessToken == null || StringUtils.isBlank(accessToken.getAccessToken()) || StringUtils.isBlank(accessToken.getAccessId())) {
                log.error("getUserDetail: invalid accessToken for ticket:{}", ticket);
                WeComUserDetail errorResponse = new WeComUserDetail();
                errorResponse.setErrcode(500);
                errorResponse.setErrmsg("Invalid access token");
                return errorResponse;
            }

            GetUserDetailRequest request = new GetUserDetailRequest(ticket);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GetUserDetailRequest> entity = new HttpEntity<>(request, headers);
            
            String url = WECOM_BASE_URL + GET_USER_DETAIL_URL;
            ResponseEntity<WeComUserDetail> resultResponse = restTemplate.exchange(
                    url, HttpMethod.POST, entity, WeComUserDetail.class, accessToken.getAccessToken());

            return handleResponse(resultResponse, "getUserDetail", ticket);
        } catch (Exception e) {
            log.error("getUserDetail: exception:{}, ticket:{}", e.getMessage(), ticket);
            log.error("getUserDetail: exception:", e);
            WeComUserDetail errorResponse = new WeComUserDetail();
            errorResponse.setErrcode(500);
            errorResponse.setErrmsg("Exception: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public WeComUserInfo getUserInfo(String agentId, String userId) {
        if (StringUtils.isBlank(userId)) {
            log.warn("getUserInfo: userId is null or empty");
            return null;
        }
        try {
            String tenantId = getTenantId();

            AccessTokenResponse accessToken = this.getAccessToken(agentId, tenantId);
            if (accessToken == null || StringUtils.isBlank(accessToken.getAccessToken()) || StringUtils.isBlank(accessToken.getAccessId())) {
                log.error("getUserInfo: invalid accessToken for userId:{}", userId);
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = WECOM_BASE_URL + USER_INFO;
            ResponseEntity<WeComUserInfo> resultResponse = restTemplate.exchange(
                    url, HttpMethod.GET, entity, WeComUserInfo.class, accessToken.getAccessToken(), userId);

            return handleResponse(resultResponse, "getUserInfo", userId);
        } catch (Exception e) {
            log.error("getUserInfo: exception:{}, userId:{}", e.getMessage(), userId);
            log.error("getUserInfo: exception:", e);
            return null;
        }
    }

    @Override
    public MessageSendResponse messageSend(String agentId, WxCpMessage message) {
        if (message == null) {
            log.error("messageSend: message is null");
            return null;
        }
        try {
            String tenantId = getTenantId();

            AccessTokenResponse accessToken = this.getAccessToken(agentId, tenantId);
            if (accessToken == null || StringUtils.isBlank(accessToken.getAccessToken()) || StringUtils.isBlank(accessToken.getAccessId())) {
                log.error("messageSend: invalid accessToken for message:{}", message);
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(message.toJson(), headers);
            
            String url = WECOM_BASE_URL + SEND_MESSAGE_URL;
            ResponseEntity<MessageSendResponse> resultResponse = restTemplate.exchange(
                    url, HttpMethod.POST, entity, MessageSendResponse.class, accessToken.getAccessToken());

            return handleResponse(resultResponse, "messageSend", message);
        } catch (Exception e) {
            log.error("messageSend: exception:{}, message:{}", e.getMessage(), message);
            log.error("messageSend: exception:", e);
            return null;
        }
    }

    @Override
    public WeComExternalContactResponse getExternalContact(String agentId, String externalUserId) {
        if (StringUtils.isBlank(externalUserId)) {
            log.error("getExternalContact: externalUserId is null or empty");
            return null;
        }
        try {
            String tenantId = getTenantId();

            AccessTokenResponse accessToken = this.getAccessToken(agentId, tenantId);
            if (accessToken == null || StringUtils.isBlank(accessToken.getAccessToken()) || StringUtils.isBlank(accessToken.getAccessId())) {
                log.error("getExternalContact: invalid accessToken for externalUserId:{}", externalUserId);
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = WECOM_BASE_URL + GET_EXTERNAL_CONTACT_URL;
            ResponseEntity<WeComExternalContactResponse> resultResponse = restTemplate.exchange(
                    url, HttpMethod.GET, entity, WeComExternalContactResponse.class, accessToken.getAccessToken(), externalUserId);

            return handleResponse(resultResponse, "getExternalContact", externalUserId);
        } catch (Exception e) {
            log.error("getExternalContact: exception:{}, externalUserId:{}", e.getMessage(), externalUserId);
            log.error("getExternalContact: exception:", e);
            return null;
        }
    }

    @Override
    public JSAPITicket getJSAPITicket(String agentId) {
        try {
            String tenantId = getTenantId();

            AccessTokenResponse accessToken = this.getAccessToken(agentId, tenantId);
            if (accessToken == null || StringUtils.isBlank(accessToken.getAccessToken()) || StringUtils.isBlank(accessToken.getAccessId())) {
                log.error("getAgentJSAPITicket: invalid accessToken for agentId:{}", agentId);
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = WECOM_BASE_URL + GET_AGENT_JSAPI_TICKET_URL;
            ResponseEntity<JSAPITicket> resultResponse = restTemplate.exchange(
                    url, HttpMethod.GET, entity, JSAPITicket.class, accessToken.getAccessToken(), "agent_config");

            return handleResponse(resultResponse, "getAgentJSAPITicket", agentId);
        } catch (Exception e) {
            log.error("getAgentJSAPITicket: exception:{}, agentId:{}", e.getMessage(), agentId);
            log.error("getAgentJSAPITicket: exception:", e);
            return null;
        }
    }

    @Override
    public JSAPITicket getWechatJSAPITicket(String agentId) {
        try {
            String tenantId = getTenantId();

            AccessTokenResponse accessToken = this.getAccessToken(agentId, tenantId);
            if (accessToken == null || StringUtils.isBlank(accessToken.getAccessToken()) || StringUtils.isBlank(accessToken.getAccessId())) {
                log.error("getWechatJSAPITicket: invalid accessToken for agentId:{}", agentId);
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = WECOM_BASE_URL + GET_WECHAT_JSAPI_TICKET_URL;
            ResponseEntity<JSAPITicket> resultResponse = restTemplate.exchange(
                    url, HttpMethod.GET, entity, JSAPITicket.class, accessToken.getAccessToken());

            return handleResponse(resultResponse, "getWechatJSAPITicket", agentId);
        } catch (Exception e) {
            log.error("getWechatJSAPITicket: exception:{}, agentId:{}", e.getMessage(), agentId);
            log.error("getWechatJSAPITicket: exception:", e);
            return null;
        }
    }

    @Override
    public WeComUserIdResponse getUserIdByEmail(String agentId, String email) {
        if (StringUtils.isBlank(email)) {
            log.error("getUserIdByEmail: email is null or empty");
            return null;
        }
        try {
            String tenantId = getTenantId();

            AccessTokenResponse accessToken = this.getAccessToken(agentId, tenantId);
            if (accessToken == null || StringUtils.isBlank(accessToken.getAccessToken()) || StringUtils.isBlank(accessToken.getAccessId())) {
                log.error("getUserIdByEmail: invalid accessToken for email:{}", email);
                return null;
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("email_type", 2);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = WECOM_BASE_URL + GET_USERID_BY_EMAIL_URL;
            ResponseEntity<WeComUserIdResponse> resultResponse = restTemplate.exchange(
                    url, HttpMethod.POST, entity, WeComUserIdResponse.class, accessToken.getAccessToken());

            return handleResponse(resultResponse, "getUserIdByEmail", email);
        } catch (Exception e) {
            log.error("getUserIdByEmail: exception:{}, email:{}", e.getMessage(), email);
            log.error("getUserIdByEmail: exception:", e);
            return null;
        }
    }

    @Override
    public WeComDepartmentUserListResponse getDepartmentUser(String agentId, String departmentId) {
        if (departmentId == null) {
            log.error("getDepartmentUserList: departmentId is null");
            return null;
        }
        try {
            String tenantId = getTenantId();

            AccessTokenResponse accessToken = this.getAccessToken(agentId, tenantId);
            if (accessToken == null || StringUtils.isBlank(accessToken.getAccessToken()) || StringUtils.isBlank(accessToken.getAccessId())) {
                log.error("getDepartmentUserList: invalid accessToken for departmentId:{}", departmentId);
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = WECOM_BASE_URL + GET_DEPARTMENT_USER_LIST_URL;
            ResponseEntity<WeComDepartmentUserListResponse> resultResponse = restTemplate.exchange(
                    url, HttpMethod.GET, entity, WeComDepartmentUserListResponse.class, accessToken.getAccessToken(), departmentId);

            return handleResponse(resultResponse, "getDepartmentUserList", departmentId);
        } catch (Exception e) {
            log.error("getDepartmentUserList: exception:{}, departmentId:{}", e.getMessage(), departmentId);
            log.error("getDepartmentUserList: exception:", e);
            return null;
        }
    }
}
