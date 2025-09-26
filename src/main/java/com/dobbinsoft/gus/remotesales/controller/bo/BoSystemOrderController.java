package com.dobbinsoft.gus.remotesales.controller.bo;

import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.aspect.log.LogRecord;
import com.dobbinsoft.gus.remotesales.aspect.log.LogRecordContext;
import com.dobbinsoft.gus.remotesales.data.dto.refund.RefundAuditDTO;
import com.dobbinsoft.gus.remotesales.data.enums.BaseEnums;
import com.dobbinsoft.gus.remotesales.data.enums.OrderRefundApproveActionEnum;
import com.dobbinsoft.gus.remotesales.data.vo.SystemRefundOrderVO;
import com.dobbinsoft.gus.remotesales.data.vo.order.OrderVO;
import com.dobbinsoft.gus.remotesales.data.vo.order.SystemOrderDetailVO;
import com.dobbinsoft.gus.remotesales.data.vo.order.SystemOrderVO;
import com.dobbinsoft.gus.remotesales.service.OrderService;
import com.dobbinsoft.gus.remotesales.service.RefundService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.ZonedDateTime;

@RestController
@Slf4j
@RequestMapping("/bo/order")
@Tag(name = "后台订单接口")
public class BoSystemOrderController {
    public static final String MODEL_ORDER_NAME = "订单列表";
    public static final String MODEL_REFUND_NAME = "退款列表";
    @Autowired
    private OrderService orderService;
    @Autowired
    private RefundService refundService;

    @GetMapping("/list")
    @Operation(summary = "获取后台订单列表")
    public R<PageResult<SystemOrderVO>> getSystemOrderList(
            @Parameter(description = "关键字(订单号/员工姓名)") @RequestParam(required = false) String keyword,
            @Parameter(description = "店铺ID")  @RequestParam(required = false) String storeId,
            @Parameter(description = "收货方式（0=客户自提, 1=物流发货）") @RequestParam(required = false) Integer receiveType,
            @Parameter(description = "配送方式（0=物流发货, 1=客户自提）") @RequestParam(required = false) Integer deliveryMethod,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "支付状态（0=未支付, 1=已支付）") @RequestParam(required = false) Integer payStatus,
            @Parameter(description = "是否退款") @RequestParam(required = false) Boolean isRefund,
            @Parameter(description = "退款筛选（0=全部退款 1=部分退款 2=无退款）") @RequestParam(required = false) Integer refundStatus,
            @Parameter(description = "开始时间") @RequestParam(required = false) ZonedDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) ZonedDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {

        return R.success(orderService.getSystemOrderList(keyword, storeId, receiveType, deliveryMethod, status, payStatus, isRefund, refundStatus, startTime, endTime, pageNum, pageSize));
    }

    @GetMapping("/refund/list")
    @Operation(summary = "获取后台退款申请订单列表")
    public R<PageResult<SystemRefundOrderVO>> getSystemRefundOrderList(
            @Parameter(description = "退款状态 （0=未退款, 1=退款成功, 2=退款失败）") @RequestParam(required = false) Integer refundStatus,
            @Parameter(description = "退款审核状态（0=待审核, 1=审核驳回, 2=审核通过）") @RequestParam(required = false) Integer approveStatus,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer orderStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.success(orderService.getSystemRefundOrderList(refundStatus, approveStatus, orderStatus, pageNum, pageSize));
    }

    @GetMapping("/detail/{orderNo}")
    @Operation(summary = "获取订单详情")
    public R<SystemOrderDetailVO> getSystemOrderDetail(@PathVariable String orderNo) {
        return R.success(orderService.getSystemOrderDetail(orderNo));
    }

    @GetMapping("/export")
    @Operation(summary = "导出系统订单列表")
    @LogRecord(modelName = MODEL_ORDER_NAME, value = "导出订单列表")
    public void exportSystemOrderList(
            @Parameter(description = "关键字(订单号/员工姓名)") @RequestParam(required = false) String keyword,
            @Parameter(description = "店铺ID")  @RequestParam(required = false) String storeId,
            @Parameter(description = "收货方式（0=客户自提, 1=物流发货）") @RequestParam(required = false) Integer receiveType,
            @Parameter(description = "配送方式（0=物流发货, 1=客户自提）") @RequestParam(required = false) Integer deliveryMethod,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "支付状态（0=未支付, 1=已支付）") @RequestParam(required = false) Integer payStatus,
            @Parameter(description = "是否退款") @RequestParam(required = false) Boolean isRefund,
            @Parameter(description = "退款筛选（0=全部退款 1=部分退款 2=无退款）") @RequestParam(required = false) Integer refundStatus,
            @Parameter(description = "开始时间") @RequestParam(required = false) ZonedDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) ZonedDateTime endTime ,
            HttpServletResponse response) {
        orderService.exportSystemOrderList(keyword, storeId, receiveType, deliveryMethod, status, payStatus, isRefund, refundStatus, startTime, endTime,response);
    }

    @GetMapping("/refund/export")
    @Operation(summary = "导出后台退款申请订单列表")
    @LogRecord(modelName = MODEL_REFUND_NAME, value = "导出退款订单列表")
    public void getSystemRefundOrderExport(
            @Parameter(description = "退款状态 （0=未退款, 1=退款成功, 2=退款失败）") @RequestParam(required = false) Integer refundStatus,
            @Parameter(description = "退款审核状态（0=待审核, 1=审核驳回, 2=审核通过）") @RequestParam(required = false) Integer approveStatus,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer orderStatus,
            HttpServletResponse response)  {
        orderService.getSystemRefundOrderExport(refundStatus, approveStatus, orderStatus, response);
    }

    @PostMapping("/refund-audit")
    @Operation(summary = "审核订单退款")
    @LogRecord(modelName = MODEL_REFUND_NAME, value = "审核订单退款: #{#action}-#{#dto.refundNo}")
    public R<OrderVO> auditRefund(@Validated @RequestBody RefundAuditDTO dto) {
        LogRecordContext.put("action", BaseEnums.getMsgByCode(dto.getAction(), OrderRefundApproveActionEnum.class));
        refundService.auditRefund(dto);
        return R.success();
    }
}
