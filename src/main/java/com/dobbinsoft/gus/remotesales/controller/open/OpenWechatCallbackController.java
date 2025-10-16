package com.dobbinsoft.gus.remotesales.controller.open;

import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.exception.AesException;
import com.dobbinsoft.gus.remotesales.utils.wx.WXBizMsgCrypt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/open/wechat-callback")
@Tag(name = "微信公众号回调接口")
public class OpenWechatCallbackController {

    @Autowired
    private ConfigCenterClient configCenterClient;

    @GetMapping
    @Operation(summary = "验证回调地址")
    public String verifyUrl(@RequestParam(value = "msg_signature", required = false) String msgSignature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr,
                            HttpServletRequest request) {
        log.info("[wechat callback] url validate: msg_signature: {}, timestamp: {}, nonce: {}, echostr: {}, request_param: {}", msgSignature, timestamp, nonce, echostr, JsonUtil.convertToString(request.getParameterMap()));
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        String decodedEchostr = URLDecoder.decode(echostr, StandardCharsets.UTF_8);
        ConfigContentVO.Secret secret = configContentVO.getSecret();
        try {
            WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(secret.getWechatToken(), secret.getWechatAesKey(), secret.getWechatAppId());
            return wxBizMsgCrypt.verifyURL(msgSignature, timestamp, nonce, decodedEchostr);
        } catch (AesException e) {
            return e.getMessage();
        }
    }


}
