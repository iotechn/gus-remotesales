package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.client.gus.logistics.model.ExpressOrderVO;
import com.dobbinsoft.gus.remotesales.data.dto.order.*;
import com.dobbinsoft.gus.remotesales.data.vo.SystemRefundOrderVO;
import com.dobbinsoft.gus.remotesales.data.vo.order.*;
import jakarta.servlet.http.HttpServletResponse;

import java.time.ZonedDateTime;

public interface OrderService {

    OrderVO submitOrder(OrderSubmitDTO submitDTO);

    /**
     * 获取客人端订单列表
     *
     * @param keyword 搜索关键字
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult<WechatOrderListVO> getWechatOrderList(String keyword, Integer pageNum, Integer pageSize);

    OrderDetailVO getWechatOrderDetail(String orderNo);

    PageResult<OrderVO> getWecomOrderList(String caId, String storeId, String regionId, WecomOrderListDTO wecomOrderListDTO);

    OrderDetailVO getWecomOrderDetail(String caId, String storeId, String regionId, String orderNo);

    void shareToCustomer(Long orderId);

    /**
     * 客人选择发货方式
     *
     * @param orderChooseDeliveryDTO
     */
    void chooseDeliveryMethod(OrderChooseDeliveryDTO orderChooseDeliveryDTO);

    /**
     * 重置物流方式
     *
     * @param orderResetDeliveryDTO
     */
    void resetDeliveryMethod(OrderResetDeliveryDTO orderResetDeliveryDTO);

    /**
     * 上传Pos no
     *
     * @param orderPosNoDTO
     */
    void uploadPosNo(OrderPosNoDTO orderPosNoDTO);

    /**
     * 订单发货
     *
     * @param orderExpressDTO
     */
    void expressOrder(OrderExpressDTO orderExpressDTO);

    /**
     * 取消订单发货
     *
     * @param orderCancelExpressDTO
     */
    void cancelExpressOrder(OrderCancelExpressDTO orderCancelExpressDTO);

    /**
     * 订单确认收货
     *
     * @param receiptDTO
     */
    void receiptOrder(OrderReceiptDTO receiptDTO);

    /**
     * 获取订单物流消息
     *
     * @param orderId
     * @return
     */
    ExpressOrderVO getOrderRouteInfo(Long orderId);

    /**
     * 获取面对面收货二维码
     *
     * @param orderId
     */
    String getFaceToFaceQrCode(Long orderId);

    /**
     * 扫描面对面收货
     *
     * @param code
     */
    void scanFaceToFaceQrCode(String code);

    /**
     * 申请发票
     *
     * @param billDTO
     */
    void applyBill(OrderBillDTO billDTO);

    /**
     * 审批调价申请
     *
     * @param adjustPriceAuditDTO
     */
    void auditAdjustPrice(OrderAdjustPriceAuditDTO adjustPriceAuditDTO);

    /**
     * 申请调价
     * @param adjustPriceApplyDTO
     */
    void applyAdjustPrice(OrderAdjustPriceApplyDTO adjustPriceApplyDTO);

    /**
     * 生成支付链接
     *
     * @param req
     * @return
     */
    CreatePayUrlVo createSelfPayUrl(CreateFriendPayLinkDTO req);

    PageResult<SystemOrderVO> getSystemOrderList(String keyword, String storeId, Integer receiveType, Integer deliveryMethod, Integer status, Integer payStatus, Boolean isRefund, Integer refundStatus, ZonedDateTime startTime, ZonedDateTime endTime, Integer pageNum, Integer pageSize);

    SystemOrderDetailVO getSystemOrderDetail(String orderNo);

    /**
     * 导出系统订单列表
     */
    void exportSystemOrderList(String keyword, String storeId, Integer receiveType, Integer deliveryMethod, Integer status, Integer payStatus, Boolean isRefund, Integer refundStatus, ZonedDateTime startTime, ZonedDateTime endTime, HttpServletResponse response);

    PageResult<SystemRefundOrderVO> getSystemRefundOrderList(Integer refundStatus, Integer approveStatus, Integer orderStatus, Integer pageNum, Integer pageSize);

    void getSystemRefundOrderExport(Integer refundStatus, Integer approveStatus, Integer orderStatus, HttpServletResponse response);
}
