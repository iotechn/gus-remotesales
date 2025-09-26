package com.dobbinsoft.gus.remotesales.controller.wecom;

import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.client.gus.logistics.model.ExpressOrderVO;
import com.dobbinsoft.gus.remotesales.data.constant.RoleTypeConstants;
import com.dobbinsoft.gus.remotesales.data.dto.order.*;
import com.dobbinsoft.gus.remotesales.data.dto.refund.RefundApplyDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.vo.order.OrderDetailVO;
import com.dobbinsoft.gus.remotesales.data.vo.order.OrderVO;
import com.dobbinsoft.gus.remotesales.service.OrderService;
import com.dobbinsoft.gus.remotesales.service.RefundService;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/wecom/order")
@Tag(name = "企业微信订单接口")
public class WecomOrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RefundService refundService;

    @PostMapping("/submit-order")
    @Operation(summary = "提交企业微信订单")
    public R<OrderVO> submitOrder(@Validated @RequestBody OrderSubmitDTO dto) {
        return R.success(orderService.submitOrder(dto));
    }

    @PostMapping("/list")
    @Operation(summary = "获取企业微信订单列表")
    public R<PageResult<OrderVO>> getWecomOrderList(@Validated @RequestBody WecomOrderListDTO dto) {
        WecomSessionInfoDTO sessionInfoDTO=SessionUtils.getWecomSession();
        if (RoleTypeConstants.SALES.equals(sessionInfoDTO.getCaPosition())) {
            return R.success(orderService.getWecomOrderList(sessionInfoDTO.getUserId(),sessionInfoDTO.getCurrentStoreId(),null,dto));
        }else if (RoleTypeConstants.STORE_MANAGER.equals(sessionInfoDTO.getCaPosition())){
            return R.success(orderService.getWecomOrderList(null,sessionInfoDTO.getCurrentStoreId(),null,dto));
        }else if (RoleTypeConstants.AREA_MANAGER.equals(sessionInfoDTO.getCaPosition())){
            return R.success(orderService.getWecomOrderList(null,null,sessionInfoDTO.getRegionId(),dto));
        }
        return R.success();
    }

    @GetMapping("/detail/{orderNo}")
    @Operation(summary = "获取企业微信订单详情")
    public R<OrderDetailVO> getWecomOrderDetail(@PathVariable String orderNo) {
            return R.success(orderService.getWecomOrderDetail(null,null,null,orderNo));
    }

    @PostMapping("/share/{orderId}")
    @Operation(summary = "分享订单给客户")
    public R<Void> shareToCustomer(@PathVariable Long orderId) {
        orderService.shareToCustomer(orderId);
        return R.success();
    }

    @PostMapping("/reset-delivery")
    @Operation(summary = "重置配送方式")
    public R<Void> resetDeliveryMethod(@Validated @RequestBody OrderResetDeliveryDTO dto) {
        orderService.resetDeliveryMethod(dto);
        return R.success();
    }

    @PostMapping("/upload-pos")
    @Operation(summary = "上传POS小票号")
    public R<Void> uploadPosNo(@Validated @RequestBody OrderPosNoDTO dto) {
        orderService.uploadPosNo(dto);
        return R.success();
    }

    @PostMapping("/express")
    @Operation(summary = "快递发货")
    public R<Void> expressOrder(@Validated @RequestBody OrderExpressDTO dto) {
        orderService.expressOrder(dto);
        return R.success();
    }

    @PostMapping("/cancel-express")
    @Operation(summary = "取消快递发货")
    public R<Void> cancelExpressOrder(@Validated @RequestBody OrderCancelExpressDTO dto) {
        orderService.cancelExpressOrder(dto);
        return R.success();
    }

    @GetMapping("/route-info/{orderId}")
    @Operation(summary = "获取订单物流路由信息")
    public R<ExpressOrderVO> getOrderRouteInfo(@PathVariable Long orderId) {
        return R.success(orderService.getOrderRouteInfo(orderId));
    }

    @PostMapping("/scan-qrcode/{code}")
    @Operation(summary = "扫描面对面提货二维码")
    public R<Void> scanFaceToFaceQrCode(@PathVariable String code) {
        orderService.scanFaceToFaceQrCode(code);
        return R.success();
    }

    @PostMapping("/refund-apply")
    @Operation(summary = "申请退款")
    public R<Void> applyRefund(@Validated @RequestBody RefundApplyDTO dto) {
        refundService.applyRefund(dto);
        return R.success();
    }

    @PostMapping("/audit-adjust-price")
    @Operation(summary = "审核调价")
    public R<Void> auditAdjustPrice(@Validated @RequestBody OrderAdjustPriceAuditDTO dto) {
        orderService.auditAdjustPrice(dto);
        return R.success();
    }

    @PostMapping("/apply-adjust-price")
    @Operation(summary = "申请调价")
    public R<Void> applyAdjustPrice(@Validated @RequestBody OrderAdjustPriceApplyDTO dto) {
        orderService.applyAdjustPrice(dto);
        return R.success();
    }
}
