package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.remotesales.data.vo.emp.EmployeeReportVO;

import java.util.List;

public interface EmployeeService {

    List<EmployeeReportVO> findEmpOrderReport(String storeId,String customerExternalUserid);
}
