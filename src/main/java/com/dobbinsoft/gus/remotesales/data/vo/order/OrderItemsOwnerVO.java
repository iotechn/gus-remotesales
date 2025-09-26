package com.dobbinsoft.gus.remotesales.data.vo.order;

import java.util.List;

public interface OrderItemsOwnerVO {

    Long getId();

    void setOrderItems(List<OrderItemVO> orderItems);

    List<OrderItemVO> getOrderItems();

}
