package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.remotesales.data.po.OrderItemPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemVO extends OrderItemPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "调价后价格")
    private BigDecimal adjustedPrice;


    public static OrderItemVO of(OrderItemPO orderItem) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtils.copyProperties(orderItem, vo);
        return vo;
    }

}
