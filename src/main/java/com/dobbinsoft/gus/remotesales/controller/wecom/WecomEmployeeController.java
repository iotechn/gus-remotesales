package com.dobbinsoft.gus.remotesales.controller.wecom;

import com.dobbinsoft.gus.remotesales.data.vo.emp.EmployeeReportVO;
import com.dobbinsoft.gus.remotesales.service.EmployeeService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RequestMapping("/wecom/employee")
@Tag(name = "wecom员工接口")
@RestController
public class WecomEmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/list")
    @Operation(summary = "获取员工列表")
    public R<List<EmployeeReportVO>> list(@RequestParam(required = false) String storeId,
                                          @RequestParam(required = false) String customerExternalUserid) {

        return R.success(employeeService.findEmpOrderReport(storeId,customerExternalUserid));
    }

}
