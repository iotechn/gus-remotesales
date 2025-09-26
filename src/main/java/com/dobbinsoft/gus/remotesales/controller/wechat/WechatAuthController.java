package com.dobbinsoft.gus.remotesales.controller.wechat;

import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.data.dto.auth.WechatAuthDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WechatSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.vo.JSSDKConfigVo;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.service.WeChatAuthService;
import com.dobbinsoft.gus.web.exception.BasicErrorCode;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/wechat/auth")
@Tag(name = "微信公众号接口")
public class WechatAuthController {

    @Autowired
    WeChatAuthService weChatAuthService;

    @GetMapping("/wechat-login")
    @Operation(summary = "微信登录(登陆后调用获取用户session信息)")
    public R<WechatSessionInfoDTO> login(WechatAuthDTO wechatAuthDTO) {
        String code = wechatAuthDTO.getCode();
        WechatSessionInfoDTO wechatSessionInfoDTO = weChatAuthService.wechatLogin(code);
        log.info("[WxLogin] get session from wechat api, openid: {}", wechatSessionInfoDTO.getOpenid());

        Integer isSnapshotuser = wechatSessionInfoDTO.getIsSnapshotuser();
        R<WechatSessionInfoDTO> r = new R<>();
        r.setCode((isSnapshotuser != null && isSnapshotuser.equals(1) ? RemotesalesErrorCode.SUCCESS_IS_SNAPSHOTUSER : BasicErrorCode.SUCCESS).getCode());
        r.setData(wechatSessionInfoDTO);
        log.info("[WxLogin] response {}", JsonUtil.convertToString(r));
        return r;
    }

    @GetMapping(value = "/get-jsapi-info")
    @Operation(summary = "获取微信JSSDK配置")
    public R<JSSDKConfigVo> jsApiInfo(@RequestParam("url") String url, @RequestParam("appId") String appId) {
        return R.success(weChatAuthService.generateJSSDKConfig(url, appId));
    }
} 