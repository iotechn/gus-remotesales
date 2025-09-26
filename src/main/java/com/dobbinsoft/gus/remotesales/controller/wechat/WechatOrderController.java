package com.dobbinsoft.gus.remotesales.controller.wechat;

import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.client.gus.logistics.model.ExpressOrderVO;
import com.dobbinsoft.gus.remotesales.data.dto.order.CreateFriendPayLinkDTO;
import com.dobbinsoft.gus.remotesales.data.dto.order.OrderBillDTO;
import com.dobbinsoft.gus.remotesales.data.dto.order.OrderChooseDeliveryDTO;
import com.dobbinsoft.gus.remotesales.data.dto.order.OrderReceiptDTO;
import com.dobbinsoft.gus.remotesales.data.vo.order.CreatePayUrlVo;
import com.dobbinsoft.gus.remotesales.data.vo.order.OrderDetailVO;
import com.dobbinsoft.gus.remotesales.data.vo.order.WechatOrderListVO;
import com.dobbinsoft.gus.remotesales.service.OrderService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/wechat/order")
@Tag(name = "微信订单接口")
public class WechatOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    @Operation(summary = "获取微信订单列表")
    public R<PageResult<WechatOrderListVO>> getWechatOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.success(orderService.getWechatOrderList(keyword, pageNum, pageSize));
    }

    @GetMapping("/detail/{orderNo}")
    @Operation(summary = "获取微信订单详情")
    public R<OrderDetailVO> getWechatOrderDetail(@PathVariable String orderNo) {
        return R.success(orderService.getWechatOrderDetail(orderNo));
    }

    @PostMapping("/choose-delivery")
    @Operation(summary = "选择配送方式")
    public R<Void> chooseDeliveryMethod(@Validated @RequestBody OrderChooseDeliveryDTO dto) {
        orderService.chooseDeliveryMethod(dto);
        return R.success();
    }

    @PostMapping("/receipt")
    @Operation(summary = "确认收货")
    public R<Void> receiptOrder(@Validated @RequestBody OrderReceiptDTO dto) {
        orderService.receiptOrder(dto);
        return R.success();
    }

    @GetMapping("/route-info/{orderId}")
    @Operation(summary = "获取订单物流路由信息")
    public R<ExpressOrderVO> getOrderRouteInfo(@PathVariable Long orderId) {
        return R.success(orderService.getOrderRouteInfo(orderId));
    }

    @GetMapping("/face-to-face-qrcode/{orderId}")
    @Operation(summary = "获取面对面提货二维码")
    public R<String> getFaceToFaceQrCode(@PathVariable Long orderId) {
        return R.success(orderService.getFaceToFaceQrCode(orderId));
    }

    @PostMapping("/apply-bill")
    @Operation(summary = "申请发票")
    public R<Void> applyBill(@Validated @RequestBody OrderBillDTO dto) {
        orderService.applyBill(dto);
        return R.success();
    }

    @Operation(summary = "创建支付连接")
    @PostMapping(value = "/create-self-pay-url")
    public R<CreatePayUrlVo> createSelfPayUrl(@RequestBody CreateFriendPayLinkDTO req) {
        CreatePayUrlVo createPayUrlVo = orderService.createSelfPayUrl(req);
        return R.success(createPayUrlVo);
    }
}
