package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.remotesales.data.dto.session.WechatSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.vo.JSSDKConfigVo;

public interface WeChatAuthService {
    JSSDKConfigVo generateJSSDKConfig(String url, String appId);

    WechatSessionInfoDTO wechatLogin(String code);
    
    WechatSessionInfoDTO getLoginSessionUserInfo(String openid);
}