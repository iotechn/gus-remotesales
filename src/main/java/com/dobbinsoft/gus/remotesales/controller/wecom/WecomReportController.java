package com.dobbinsoft.gus.remotesales.controller.wecom;

import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.data.vo.customer.OrderCustomerVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.SalesPerformanceByKeyWordsVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.SalesPerformanceVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.StorePerformanceVo;
import com.dobbinsoft.gus.remotesales.service.ReportService;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wecom/report")
@Slf4j
@Tag(name = "企业微信报表接口")
public class WecomReportController {

    @Autowired
    ReportService reportService;

    @GetMapping("/ca/performance")
    @Operation(summary = "查询CA员工业绩")
    public R<Map<String, SalesPerformanceVo>> caPerformance() {
        return R.success(reportService.caPerformance());
    }

    @GetMapping("/sm/store/{storeId}")
    @Operation(summary = "查询店铺业绩")
    public R<Map<String, SalesPerformanceVo>> smStoreReport(@PathVariable("storeId") String storeId) {
        return R.success(reportService.getSmStoreReport(storeId));
    }

    @GetMapping("/sm/store/smTrendReportByDay/{storeId}")
    @Operation(summary = "店铺下15日趋势")
    public R<Map<String, SalesPerformanceVo>> smStoreReportByDay(@PathVariable("storeId") String storeId) {
        return R.success(reportService.trendReportByDay(storeId, null, 15));
    }

    @GetMapping("/sm/store/smStoreReportByCa/{storeId}")
    @Operation(summary = "查看店铺下CA业绩")
    public R<Map<String, List<SalesPerformanceByKeyWordsVo>>> smStoreReportByCa(@PathVariable("storeId") String storeId) {
        return R.success(reportService.smStoreReportByCa(storeId));
    }

    @GetMapping("/rm/region")
    @Operation(summary = "查看当前登录用户的区域业绩")
    public R<Map<String, SalesPerformanceVo>> rmRegionReport() {
        return R.success(reportService.getRmStoreReport());
    }

    @GetMapping("/rm/region/rmTrendReportByDay")
    @Operation(summary = "区域业绩15日趋势")
    public R<Map<String, SalesPerformanceVo>> rmTrendReportByDay() {
        return R.success(reportService.trendReportByDay(null, SessionUtils.getWecomSession().getRegionId(), 15));
    }

    @GetMapping("/rm/region/rmRegionReportByStore")
    @Operation(summary = "查看当前登录用户的区域下的店铺业绩")
    public R<Map<String, List<SalesPerformanceByKeyWordsVo>>> rmRegionReportByStore() {
        return R.success(reportService.rmRegionReportByStore());
    }

    @GetMapping("/my-order-customer")
    @Operation(summary = "我的顾客(如果storeId不传 默认为当前登录员工的顾客）")
    public R<PageResult<OrderCustomerVo>> orderCustomer(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String storeId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.success(reportService.orderCustomer(keyword, storeId, pageNum, pageSize));
    }
    @GetMapping("/customer-detail/{customerExternalUserid}")
    @Operation(summary = "我的顾客-详情")
    public R<OrderCustomerVo> orderCustomer(@PathVariable String customerExternalUserid) {
        return R.success(reportService.customerDetail(customerExternalUserid));
    }
    @GetMapping("/sm/store-performance/{storeId}")
    @Operation(summary = "SM查询店铺详情")
    public R<StorePerformanceVo> smStorePerformance(@PathVariable("storeId") String storeId) {
        return R.success(reportService.smStorePerformance(storeId));
    }

    @GetMapping("/rm/store-performance/{storeId}")
    @Operation(summary = "RM查询店铺详情")
    public R<StorePerformanceVo> rmStorePerformance(@PathVariable("storeId") String storeId) {
        return R.success(reportService.rmStorePerformance(storeId));
    }
}
