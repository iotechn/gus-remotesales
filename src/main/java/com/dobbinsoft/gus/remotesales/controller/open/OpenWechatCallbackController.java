package com.dobbinsoft.gus.remotesales.controller.open;

import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.utils.SignUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/open/wechat-callback")
@Tag(name = "微信公众号回调接口")
public class OpenWechatCallbackController {

    @Autowired
    private ConfigCenterClient configCenterClient;

    @GetMapping
    @Operation(summary = "验证回调地址")
    public String verifyUrl(@RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr,
                            HttpServletRequest request) {
        log.info("[wechat callback] url validate: request_param: {}", JsonUtil.convertToString(request.getParameterMap()));
        
        try {
            // 获取微信配置信息
            ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
            ConfigContentVO.Secret secret = configContentVO.getSecret();
            String wechatToken = secret.getWechatToken();
            
            // 验证签名
            if (SignUtil.checkWechatSignature(signature, timestamp, nonce, wechatToken)) {
                log.info("[wechat callback] signature verification passed, returning echostr: {}", echostr);
                return echostr;
            } else {
                log.warn("[wechat callback] signature verification failed");
                return "signature verification failed";
            }
        } catch (Exception e) {
            log.error("[wechat callback] verification error", e);
            return "verification error";
        }
    }


}
