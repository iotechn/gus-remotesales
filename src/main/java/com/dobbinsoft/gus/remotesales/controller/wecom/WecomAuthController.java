package com.dobbinsoft.gus.remotesales.controller.wecom;

import com.dobbinsoft.gus.remotesales.data.dto.auth.WecomAuthDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.vo.JSSDKConfigVo;
import com.dobbinsoft.gus.remotesales.service.WecomAuthService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/wecom/auth")
@Tag(name = "企业微信鉴权接口")
public class WecomAuthController {
    @Autowired
    WecomAuthService wecomAuthService;

    @PostMapping("/wecom-login")
    @Operation(summary = "企业微信登录OAuth")
    public R<WecomSessionInfoDTO> login(@Valid @RequestBody WecomAuthDTO wecomAuthDTO) {
        return R.success(wecomAuthService.login(wecomAuthDTO));
    }

    @GetMapping(value = "/get-jsapi-info")
    @Operation(summary = "获取WecomTicket")
    public R<JSSDKConfigVo> jsApiInfo(@RequestParam String url) {
        return R.success(wecomAuthService.generateJSSDKConfig(url));
    }

    // 企业微信H5，也需要调用wxjsdk，所以需要进行两次config
    @GetMapping(value = "/get-wechat-jsapi-info")
    @Operation(summary = "获取微信JSSDK配置")
    public R<JSSDKConfigVo> jsWechatApiInfo(@RequestParam("url") String url) {
        return R.success(wecomAuthService.generateWechatJSSDKConfig(url));
    }


    @PutMapping(value = "/switch-user-info")
    @Operation(summary = "切换角色信息 userRole: STORE_MANAGER ,SALES")
    public R<WecomSessionInfoDTO> switchUserInfo(@RequestParam("storeId") String storeId,
                                                 @RequestParam("userRole") String userRole
    ) {
        return R.success(wecomAuthService.switchUserInfo(storeId,userRole));
    }

    @GetMapping("/rm/store-performance/list")
    @Operation(summary = "RM查询当前登陆人店铺列表")
    public R<List<WecomSessionInfoDTO.Store>> rmStorePerformanceList() {
        return R.success(wecomAuthService.rmStorePerformanceList());
    }

}
