package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.data.po.OperationLogPO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 系统日志服务接口
 */
public interface SystemLogService {

    List<String> getAllLogModelName();
    PageResult<OperationLogPO> pageList(Integer pageNum, Integer pageSize, String  modelName, ZonedDateTime startTime, ZonedDateTime endTime, String keyword);

    /**
     * 导出系统日志
     *
     * @param response HTTP响应
     * @param modelName 模块名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param keyword 关键字
     * @throws IOException IO异常
     */
    void exportExcel(HttpServletResponse response, String modelName, ZonedDateTime startTime, ZonedDateTime endTime, String keyword) throws IOException;
} 