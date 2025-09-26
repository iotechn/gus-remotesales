package com.dobbinsoft.gus.remotesales.controller;

import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.service.ConfigCenterService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/config-center")
@RequiredArgsConstructor
@Tag(name = "动态表单配置")
public class ConfigCenterController {
    private final ConfigCenterService configCenterService;

    @GetMapping("/get-config")
    @Operation(summary = "获取动态表单配置")
    public R<ConfigContentVO> getBrandAllConfigContent() {
        return R.success(configCenterService.getBrandAllConfigContent());
    }

} 