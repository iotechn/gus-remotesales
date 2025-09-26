package com.dobbinsoft.gus.remotesales.client.wechat;

import com.dobbinsoft.gus.remotesales.client.wechat.model.WeChatUserInfo;
import com.dobbinsoft.gus.remotesales.client.wechat.model.WechatUserToken;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.JSAPITicket;

public interface WeChatClient {

    JSAPITicket generateJSSDKConfig(String jsapi, String appId);

    WechatUserToken getUserToken(String code);

    WeChatUserInfo getUserInfo(String accessToken, String openId);

}
