package com.dobbinsoft.gus.remotesales.controller.open;

import com.dobbinsoft.gus.remotesales.data.vo.basic.BaseEnumVO;
import com.dobbinsoft.gus.remotesales.service.BasicService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "基础接口")
@RequestMapping("/open/basic")
@RestController
public class OpenBasicController {

    @Autowired
    private BasicService basicService;


    @GetMapping("/enums")
    @Operation(summary = "获取系统内枚举")
    public R<List<BaseEnumVO>> enums() {
        return R.success(basicService.enums());
    }
}
