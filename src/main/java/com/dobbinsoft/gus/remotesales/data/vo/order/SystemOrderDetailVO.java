package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class SystemOrderDetailVO extends SystemOrderVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "xstoreId")
    private String xstoreId;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "内部备注")
    private String innerRemark;
    @Schema(description = "商品明细")
    private List<OrderItemVO> orderItems;
    @Schema(description = "收货地址")
    private OrderAddressVO addressVo;
    @Schema(description = "票据")
    private OrderBillVO billVo;
    private ExpressNumberVO expressNumber;
    @Schema(description = "提货店铺")
    private OrderPickupStoreVO pickupStoreVo;
    @Schema(description = "支付")
    private PaymentVO paymentVO;
    @Schema(description = "订单日志")
    private List<OrderLogVO> orderLogs;
    @Schema(description = "订单支付日志")
    private List<OrderPayLogVO> orderPayLogs;

    public static SystemOrderDetailVO of(OrderPO order) {
        SystemOrderDetailVO vo = new SystemOrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        if (StringUtils.isNotBlank(order.getAddress())) {
            vo.setAddressVo(JsonUtil.convertToObject(order.getAddress(), OrderAddressVO.class));
        }
        return vo;
    }


}
