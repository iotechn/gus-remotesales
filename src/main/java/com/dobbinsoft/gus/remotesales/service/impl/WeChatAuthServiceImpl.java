package com.dobbinsoft.gus.remotesales.service.impl;

import com.dobbinsoft.gus.common.utils.context.GenericRequestContextHolder;
import com.dobbinsoft.gus.common.utils.context.bo.TenantContext;
import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.client.wechat.WeChatClient;
import com.dobbinsoft.gus.remotesales.client.wechat.model.WeChatUserInfo;
import com.dobbinsoft.gus.remotesales.client.wechat.model.WechatUserToken;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.JSAPITicket;
import com.dobbinsoft.gus.remotesales.data.dto.session.WechatSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.vo.JSSDKConfigVo;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.service.WeChatAuthService;
import com.dobbinsoft.gus.remotesales.utils.SignUtil;
import com.dobbinsoft.gus.web.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WeChatAuthServiceImpl implements WeChatAuthService {
    private static final String REDIS_CACHE_WECHAT_USER_INFO = "CUS:WECHAT:LOGIN:USER:INFO:";
    private static final String REDIS_CACHE_JS_TICKET_CACHE = "CUS:WECHAT:JS_TICKET:";
    private final static long EXPIRES_CLOSE = 30;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WeChatClient weChatClient;


    @Override
    public JSSDKConfigVo generateJSSDKConfig(String url, String appId) {
        // 构建JSAPI Ticket的缓存key
        String cacheKey = REDIS_CACHE_JS_TICKET_CACHE.concat(getTenantId()).concat(":").concat(appId);

        // 尝试从缓存中获取JSAPI Ticket
        String cacheDataStr = redisTemplate.opsForValue().get(cacheKey);
        JSAPITicket apiTicketResult;

        if (StringUtils.isNotEmpty(cacheDataStr)) {
            // 如果缓存中存在，则直接使用缓存的Ticket
            apiTicketResult = JsonUtil.convertValue(cacheDataStr, JSAPITicket.class);
        } else {
            // 如果缓存中不存在，则重新获取Ticket并缓存
            apiTicketResult = weChatClient.generateJSSDKConfig("jsapi", appId);
            if(Objects.isNull(apiTicketResult)){
                log.error("generateJSSDKConfig fail :url:{},appId:{}",url,appId);
                throw new ServiceException(RemotesalesErrorCode.SYSTEM_ERROR);
            }
            // 设置缓存过期时间为Ticket有效期减去30秒，避免临界点问题
            if (apiTicketResult.getExpiresIn() > EXPIRES_CLOSE) {
                redisTemplate.opsForValue().set(cacheKey, JsonUtil.convertToString(apiTicketResult),
                        apiTicketResult.getExpiresIn() - EXPIRES_CLOSE, TimeUnit.SECONDS);
            }
        }

        // 创建JSSDK配置对象
        return SignUtil.assembleConfigVo(url, apiTicketResult);
    }


    @Override
    public WechatSessionInfoDTO wechatLogin(String code) {
        WechatSessionInfoDTO sessionInfoDTO = new WechatSessionInfoDTO();
        // 1. 获取user token
        WechatUserToken userToken = weChatClient.getUserToken(code);
        // 2. 如果scope是详情信息，则获取详细UserInfo
        if(Objects.isNull(userToken)||StringUtils.isEmpty(userToken.getOpenid())) {
            log.error("wechatClient.getUserInfo return null :openID:{}",userToken.getOpenid());
            throw  new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
        }
        WeChatUserInfo userInfo = weChatClient.getUserInfo(userToken.getAccessToken(), userToken.getOpenid());
        sessionInfoDTO.setOpenid(userInfo.getOpenId());
        sessionInfoDTO.setUnionid(userInfo.getUnionId());
        sessionInfoDTO.setNickname(userInfo.getNickname());
        sessionInfoDTO.setHeadImgUrl(userInfo.getHeadImgUrl());
        sessionInfoDTO.setIsSnapshotuser(userToken.getIsSnapshotuser());
        
        // 缓存session信息，默认缓存1天
        redisTemplate.opsForValue()
                .set(REDIS_CACHE_WECHAT_USER_INFO.concat(userInfo.getOpenId()), JsonUtil.convertToString(sessionInfoDTO), Duration.ofDays(1));
        
        return sessionInfoDTO;
    }

    @Override
    public WechatSessionInfoDTO getLoginSessionUserInfo(String openid) {
        if (StringUtils.isEmpty(openid)) {
            log.error("getWechatUserInfo: openid is null or empty");
            return null;
        }
        String key = REDIS_CACHE_WECHAT_USER_INFO.concat(openid);
        String value = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(value)) {
            log.error("get WeChat LoginUserInfo is null :key:{}", key);
            return null;
        }
        // 访问时自动续约至一天
        redisTemplate.expire(key, Duration.ofDays(1));
        return JsonUtil.convertValue(value, WechatSessionInfoDTO.class);
    }

    private String getTenantId() {
        return GenericRequestContextHolder.getTenantContext()
                .map(TenantContext::getTenantId)
                .orElseThrow(() -> new ServiceException(RemotesalesErrorCode.SYSTEM_ERROR));
    }


}