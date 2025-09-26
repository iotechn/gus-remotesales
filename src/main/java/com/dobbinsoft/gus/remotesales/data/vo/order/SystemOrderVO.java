package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
public class SystemOrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单提交日期")
    private ZonedDateTime submitTime;
    @Schema(description = "订单编号")
    private String orderNo;
    @Schema(description = "CA员工的WWID")
    private String caWwid;
    @Schema(description = "CA员工姓名")
    private String caName;
    @Schema(description = "店铺名称")
    private String storeName;
    @Schema(description = "用户真实姓名")
    private String customerName;
    @Schema(description = "订单状态（0=待转发, 10=待付款, 20=重置物流中, 30=待发货, 40=待提货, 50=待收货, 60=已完成, 70=已过期, -1=未知）")
    private Integer status;
    @Schema(description = "POS Number")
    private String posNumber;
    @Schema(description = "收货方式（0=客户自提, 1=物流发货）")
    private Integer receiveType;
    @Schema(description ="小票单号")
    private String receiptNumber;
    @Schema(description = "订单总金额")
    private BigDecimal amount;
    @Schema(description = "配送方式（0=物流发货, 1=客户自提）")
    private Integer deliveryMethod;
    @Schema(description = "配送方式选择时间（最后）")
    private ZonedDateTime deliveryMethodChooseTime;
    @Schema(description = "物流公司名称")
    private String logisticsCompany;
    @Schema(description = "物流单号")
    private String logisticsNo;
    @Schema(description = "支付金额")
    private BigDecimal payAmount;
    public static SystemOrderVO of(OrderPO orderPO) {
        SystemOrderVO vo=new SystemOrderVO();
        BeanUtils.copyProperties(orderPO, vo);
        return vo;
    }
}
