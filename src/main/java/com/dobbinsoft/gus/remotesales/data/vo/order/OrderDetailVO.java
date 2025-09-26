package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.data.constant.RSConstants;
import com.dobbinsoft.gus.remotesales.data.enums.DeliveryMethodEnum;
import com.dobbinsoft.gus.remotesales.data.enums.OrderStatusEnum;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDetailVO extends OrderVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String UNKNOWN = "未知";

    @Schema(description = "调价后价格")
    private BigDecimal adjustedAmount;

    @Schema(description = "调价记录对象列表")
    private List<OrderAdjustPriceVO> adjustPriceVos;

    @Schema(description = "订单退款对象列表")
    private List<OrderRefundVO> orderRefundVos;

    @Schema(description = "订单支付记录对象列表 分笔会多")
    private List<OrderPayLogVO> orderPayLogVos;

    @Schema(description = "收货地址")
    private OrderAddressVO addressVo;

    @Schema(description = "票据")
    private OrderBillVO billVo;

    @Schema(description = "提货店铺")
    private OrderPickupStoreVO pickupStoreVo;

    @Schema(description = "自动收货剩余时长(秒)")
    private Integer autoReceiveSecond;


    public static OrderDetailVO of(OrderPO order) {
        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    public void convertFormat(Integer autoReceiveSeconds) {
        this.setAddressVo(JsonUtil.convertToObject(StringUtils.firstNonBlank(this.getAddress(), RSConstants.EMPTY_JSON_OBJECT), OrderAddressVO.class));
        this.setBillVo(JsonUtil.convertToObject(StringUtils.firstNonBlank(this.getBill(), RSConstants.EMPTY_JSON_OBJECT), OrderBillVO.class));
        if (StringUtils.isEmpty(this.getCustomerUnionid())) {
            this.setCustomerName(UNKNOWN);
            this.setCustomerNickname(UNKNOWN);
            this.setCustomerMobile(UNKNOWN);
        }

        if (this.getDeliveryMethod() != null
                && this.getDeliveryMethod().equals(DeliveryMethodEnum.LOGISTICS.getCode())
                && this.getStatus().equals(OrderStatusEnum.TO_RECEIVE.getCode())) {
            ZonedDateTime autoReceiveTime = this.getDeliveryTime().plusSeconds(autoReceiveSeconds);
            this.setAutoReceiveSecond(Math.min(0, (int) Duration.between(ZonedDateTime.now(), autoReceiveTime).getSeconds()));
        }
    }

}
