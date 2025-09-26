package com.dobbinsoft.gus.remotesales.service.biz;

import com.dobbinsoft.gus.remotesales.data.dto.session.WechatSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.mapper.OrderMapper;
import com.dobbinsoft.gus.web.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderBizService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 获取订单，并校验权限
     * @param orderId
     * @param wecomSession
     * @return
     */
    public OrderPO getOrderByCA(Long orderId, WecomSessionInfoDTO wecomSession) {
        // 查询订单
        OrderPO order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }
        // 权限校验：仅订单所属CA可操作
        if (!wecomSession.getUserId().equals(order.getCaWwid())) {
            throw new ServiceException(RemotesalesErrorCode.NO_PERMISSION);
        }
        return order;
    }

    /**
     * 获取订单，并校验是否属于用户
     * @param orderId
     * @param wechatSession
     * @return
     */
    public OrderPO getOrderByCustomer(Long orderId, WechatSessionInfoDTO wechatSession) {
        OrderPO order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }

        // 权限校验
        if (!wechatSession.getUnionid().equals(order.getCustomerUnionid())) {
            throw new ServiceException(RemotesalesErrorCode.NO_PERMISSION);
        }
        return order;
    }

}
