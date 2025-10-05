package com.dobbinsoft.gus.remotesales.controller.open;

import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.data.dto.auth.WechatAuthDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WechatSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.service.WeChatAuthService;
import com.dobbinsoft.gus.web.exception.BasicErrorCode;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("/open/wechat-auth")
public class OpenWechatAuthController {

    @Autowired
    WeChatAuthService weChatAuthService;

    @PostMapping("/login")
    @Operation(summary = "微信登录(登陆后调用获取用户session信息)")
    public R<WechatSessionInfoDTO> login(@Valid @RequestBody WechatAuthDTO wechatAuthDTO) {
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

}
