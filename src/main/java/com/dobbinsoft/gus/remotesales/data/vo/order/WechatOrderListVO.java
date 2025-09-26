package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(description = "客人端订单列表展示对象")
@Data
public class WechatOrderListVO implements OrderItemsOwnerVO{
    @Schema(description = "订单ID")
    private Long id;
    @Schema(description = "订单编号")
    private String orderNo;
    @Schema(description = "订单状态（0=待转发, 10=待付款, 20=重置物流中, 30=待发货, 40=待提货, 50=待收货, 60=已完成, 70=已过期, -1=未知）")
    private Integer status;
    @Schema(description = "订单状态描述")
    private String statusDesc;
    @Schema(description = "订单总金额")
    private BigDecimal amount;
    @Schema(description = "订单总金额字符串格式")
    private String amountStr;
    @Schema(description = "订单商品明细列表")
    private List<OrderItemVO> orderItems;
    @Schema(description = "CA员工WWID")
    private String caWwid;
    @Schema(description = "订单创建时间")
    private ZonedDateTime createdDate;

    public static WechatOrderListVO of(OrderPO order) {
        WechatOrderListVO vo = new WechatOrderListVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

}
