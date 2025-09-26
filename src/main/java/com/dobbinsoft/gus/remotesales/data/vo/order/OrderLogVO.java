package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.remotesales.data.po.OrderLogPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
public class OrderLogVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "订单状态（0=待转发, 10=待付款, 20=重置物流中, 30=待发货, 40=待提货, 50=待收货, 60=已完成, 70=已过期, -1=未知）")
    private Integer status;

    @Schema(description = "订单状态描述")
    private String statusDesc;
    @Schema(description = "日志描述")
    private String description;
    @Schema(description = "订单日志类型（参照枚举 Type：0=提交订单, 10=分享订单, 20=付款成功, 30=修改订单配送方式, 40=自提货物扫码核销, 50=销售发货, 55=修改物流信息, 60=确认收货, 70=订单超过24小时未支付过期, 80=订单退款申请, 81=订单退款审核通过, 82=订单退款审核驳回, 83=系统退款中, 84=订单退款失败, 85=订单退款成功, -1=未知）")
    private Integer type;
    @Schema(description = "订单日志类型描述")
    private String typeDesc;
    @Schema(description = "备注（UTF-8编码，二进制比较）")
    private String comment;
    @Schema(description = "执行人名字")
    private String createdByName;
    @Schema(description = "创建时间")
    private ZonedDateTime createdDate;

    public static OrderLogVO of(OrderLogPO orderLogPO) {
        OrderLogVO orderLogVO = new OrderLogVO();
        BeanUtils.copyProperties(orderLogPO, orderLogVO);
        return orderLogVO;
    }

}
