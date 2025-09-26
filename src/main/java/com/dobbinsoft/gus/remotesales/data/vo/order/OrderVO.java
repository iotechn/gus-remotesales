package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.remotesales.data.po.OrderItemPO;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderVO extends OrderPO implements OrderItemsOwnerVO {

    private List<OrderItemVO> orderItems;

    public static OrderVO of(OrderPO order) {
        OrderVO vo = new OrderVO();
        // 拷贝OrderPO属性
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    public static OrderVO of(OrderPO order, List<OrderItemPO> orderItemPOList) {
        OrderVO vo = of(order);
        // 拷贝OrderItemPO到OrderItemVO
        if (orderItemPOList != null) {
            List<OrderItemVO> itemVOList = new ArrayList<>();
            for (OrderItemPO itemPO : orderItemPOList) {
                OrderItemVO itemVO = new OrderItemVO();
                BeanUtils.copyProperties(itemPO, itemVO);
                itemVOList.add(itemVO);
            }
            vo.setOrderItems(itemVOList);
        }
        return vo;
    }

}
