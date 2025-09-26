package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.remotesales.data.po.OrderRefundPO;
import org.springframework.beans.BeanUtils;
public class OrderRefundVO extends OrderRefundPO {

    // 工厂方法
    public static OrderRefundVO of(OrderRefundPO orderRefundPO) {
        OrderRefundVO orderRefundVO = new OrderRefundVO();
        BeanUtils.copyProperties(orderRefundPO, orderRefundVO);
        return orderRefundVO;
    }
    
}
