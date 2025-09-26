package com.dobbinsoft.gus.remotesales.controller.bo;

import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.aspect.log.LogRecord;
import com.dobbinsoft.gus.remotesales.data.po.OperationLogPO;
import com.dobbinsoft.gus.remotesales.service.SystemLogService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 系统日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/bo/system/logs")
@Tag(name = "后台日志接口")
public class BoSystemLogController {
    public static final String MODEL_NAME = "系统日志";
    @Autowired
    private SystemLogService systemLogService;

    @GetMapping("/page")
    @Operation(summary = "分页查询系统日志")
    public R<PageResult<OperationLogPO>> page(
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) ZonedDateTime startTime,
            @RequestParam(required = false) ZonedDateTime endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.success(systemLogService.pageList(pageNum, pageSize, modelName, startTime, endTime, keyword));
    }

    @GetMapping("/export")
    @Operation(summary = "导出系统日志")
    @LogRecord(modelName = MODEL_NAME, value = "导出系统日志")
    public void exportExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) ZonedDateTime startTime,
            @RequestParam(required = false) ZonedDateTime endTime,
            @RequestParam(required = false) String keyword) throws IOException {
        systemLogService.exportExcel(response, modelName, startTime, endTime, keyword);
    }

    @GetMapping("/get-all-model-name")
    @Operation(summary = "获取全部模块名称")
    public R<List<String>> getAllLogModelName() {
        return R.success(systemLogService.getAllLogModelName());
    }
}
