package com.dobbinsoft.gus.remotesales.controller.open;

import com.dobbinsoft.gus.remotesales.data.dto.auth.WecomAuthDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.service.WecomAuthService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("/open/wecom-auth")
public class OpenWecomAuthController {

    @Autowired
    WecomAuthService wecomAuthService;

    @PostMapping("/login")
    @Operation(summary = "企业微信登录OAuth")
    public R<WecomSessionInfoDTO> login(@Valid @RequestBody WecomAuthDTO wecomAuthDTO) {
        return R.success(wecomAuthService.login(wecomAuthDTO));
    }


}
