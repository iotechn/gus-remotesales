package com.dobbinsoft.gus.remotesales.client.wechat;

import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.client.wechat.model.WeChatUserInfo;
import com.dobbinsoft.gus.remotesales.client.wechat.model.WechatUserToken;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.JSAPITicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WeChatClientImpl implements WeChatClient {

    private static final String WECHAT_BASE_URL = "https://api.weixin.qq.com";
    private static final String GET_USER_TOKEN_URL = "/sns/oauth2/access_token?appid={appId}&secret={secret}&code={code}&grant_type=authorization_code";
    private static final String GET_USER_INFO_URL = "/sns/userinfo?access_token={accessToken}&openid={openId}&lang=zh_CN";

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ConfigCenterClient configCenterClient;


    @Override
    public JSAPITicket generateJSSDKConfig(String jsapi, String appId) {
        return null;
    }

    @Override
    public WechatUserToken getUserToken(String code) {
        try {
            // 从配置中心获取微信配置
            ConfigContentVO config = configCenterClient.getBrandAllConfigContent();
            if (config == null || config.getSecret() == null) {
                throw new RuntimeException("微信配置未找到");
            }
            
            String appId = config.getSecret().getWechatAppId();
            String appSecret = config.getSecret().getWechatAppSecret();
            
            if (appId == null || appSecret == null) {
                throw new RuntimeException("微信AppId或AppSecret未配置");
            }
            
            // 构建请求URL
            String url = WECHAT_BASE_URL + GET_USER_TOKEN_URL;
            
            // 调用微信API获取用户token
            WechatUserToken userToken = restTemplate.getForObject(url, WechatUserToken.class, appId, appSecret, code);
            
            if (userToken == null) {
                throw new RuntimeException("获取微信用户token失败");
            }
            
            return userToken;
            
        } catch (Exception e) {
            throw new RuntimeException("获取微信用户token异常: " + e.getMessage(), e);
        }
    }

    @Override
    public WeChatUserInfo getUserInfo(String accessToken, String openId) {
        try {
            // 构建请求URL
            String url = WECHAT_BASE_URL + GET_USER_INFO_URL;
            
            // 调用微信API获取用户信息
            WeChatUserInfo userInfo = restTemplate.getForObject(url, WeChatUserInfo.class, accessToken, openId);
            
            if (userInfo == null) {
                throw new RuntimeException("获取微信用户信息失败");
            }
            
            return userInfo;
            
        } catch (Exception e) {
            throw new RuntimeException("获取微信用户信息异常: " + e.getMessage(), e);
        }
    }

}
