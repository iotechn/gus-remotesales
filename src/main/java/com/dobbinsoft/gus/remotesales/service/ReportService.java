package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.data.vo.customer.OrderCustomerVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.SalesPerformanceByKeyWordsVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.SalesPerformanceVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.StorePerformanceVo;

import java.util.List;
import java.util.Map;

public interface ReportService {
    Map<String, SalesPerformanceVo> getSmStoreReport(String storeId);

    Map<String, SalesPerformanceVo> trendReportByDay(String storeId, String regionId, Integer dayRange);

    Map<String, List<SalesPerformanceByKeyWordsVo>> smStoreReportByCa(String storeId);

    Map<String, SalesPerformanceVo> getRmStoreReport();

    Map<String, List<SalesPerformanceByKeyWordsVo>> rmRegionReportByStore();

    PageResult<OrderCustomerVo> orderCustomer(String keyword,String storeId, Integer pageNum, Integer pageSize);

    Map<String, SalesPerformanceVo> caPerformance();

    StorePerformanceVo smStorePerformance(String storeId);
    StorePerformanceVo rmStorePerformance(String storeId);

    OrderCustomerVo customerDetail(String customerUnionid);
}
