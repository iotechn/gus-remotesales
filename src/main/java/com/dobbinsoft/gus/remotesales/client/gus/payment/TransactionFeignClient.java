package com.dobbinsoft.gus.remotesales.client.gus.payment;

import com.dobbinsoft.gus.remotesales.client.gus.payment.model.*;
import com.dobbinsoft.gus.remotesales.configuration.OpenFeignConfig;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "payment-transaction",
        url = "http://gus-payment",
        path = "/api/transaction",
        configuration = OpenFeignConfig.class)
public interface TransactionFeignClient {

    @Operation(summary = "创建预支付订单", description = "创建预支付订单，返回支付参数")
    @PostMapping("/prepay")
    R<Object> prepay(
            @Parameter(description = "创建交易请求参数", required = true)
            @Validated @RequestBody TransactionCreateDTO createDTO);

    @Operation(summary = "获取交易详情", description = "获取交易详情，如果本地数据与远程不一致会自动同步")
    @PostMapping("/detail")
    R<TransactionVO> getTransactionDetail(
            @Parameter(description = "获取交易请求参数", required = true)
            @RequestBody TransactionGetDTO getDTO);

    @Operation(summary = "申请退款", description = "申请退款，支持部分退款和全额退款")
    @PostMapping("/refund")
    R<TransactionRefundVO> refund(
            @Parameter(description = "退款请求参数", required = true)
            @Validated @RequestBody TransactionRefundDTO refundDTO);

    @Operation(summary = "获取退款详情", description = "获取退款详情，如果本地数据与远程不一致会自动同步")
    @PostMapping("/refund/detail")
    R<TransactionRefundVO> getRefundDetail(
            @Parameter(description = "获取退款请求参数", required = true)
            @RequestBody TransactionRefundGetDTO getDTO);

}
