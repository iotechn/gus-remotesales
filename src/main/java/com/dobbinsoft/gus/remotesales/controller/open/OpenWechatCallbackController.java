package com.dobbinsoft.gus.remotesales.controller.open;

import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.data.dto.wechat.WechatNotifyVerifyDTO;
import com.dobbinsoft.gus.remotesales.exception.AesException;
import com.dobbinsoft.gus.remotesales.utils.wx.WXBizMsgCrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/open/wechat-callback")
public class OpenWechatCallbackController {

    @Autowired
    private ConfigCenterClient configCenterClient;

    @GetMapping
    public String verifyUrl(WechatNotifyVerifyDTO wechat) {
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        wechat.setEchostr(URLDecoder.decode(wechat.getEchostr(), StandardCharsets.UTF_8));
        ConfigContentVO.Secret secret = configContentVO.getSecret();
        try {
            WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(secret.getWechatToken(), secret.getWechatAesKey(), secret.getWechatAppId());
            return wxBizMsgCrypt.verifyURL(wechat.getMsgSignature(), wechat.getTimestamp(), wechat.getNonce(), wechat.getEchostr());
        } catch (AesException e) {
            return e.getMessage();
        }
    }


}
