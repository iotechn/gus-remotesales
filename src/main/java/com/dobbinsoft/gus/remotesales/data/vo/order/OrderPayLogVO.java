package com.dobbinsoft.gus.remotesales.data.vo.order;

import com.dobbinsoft.gus.remotesales.data.po.OrderPayLogPO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class OrderPayLogVO extends OrderPayLogPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static OrderPayLogVO of(OrderPayLogPO orderPayLogPO) {
        OrderPayLogVO orderPayLogVO = new OrderPayLogVO();
        BeanUtils.copyProperties(orderPayLogPO, orderPayLogVO);
        return orderPayLogVO;
    }

}
