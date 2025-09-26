package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.remotesales.data.po.OrderAdjustPricePO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
@Getter
@Setter
public class OrderAdjustPriceVO extends OrderAdjustPricePO {

    // 工厂方法
    public static OrderAdjustPriceVO of(OrderAdjustPricePO orderAdjustPricePO) {
        OrderAdjustPriceVO orderAdjustPriceVO = new OrderAdjustPriceVO();
        BeanUtils.copyProperties(orderAdjustPricePO, orderAdjustPriceVO);
        return orderAdjustPriceVO;
    }
}
