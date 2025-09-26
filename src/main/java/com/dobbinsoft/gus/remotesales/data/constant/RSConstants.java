package com.dobbinsoft.gus.remotesales.data.constant;

import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.data.po.OrderItemPO;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import com.dobbinsoft.gus.remotesales.data.vo.order.OrderAddressVO;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

public class RSConstants {

    public static final String EMPTY_JSON_OBJECT = "{}";

    public static final String ORDER_QR_CODE_CACHE_KEY = "ORDER_QR_CODE_CACHE_KEY:";
    public static final int QR_CODE_EXPIRE_SECONDS = 100;


    public static final String ORDER_EMAIL_TEMPLATE =
            """
                    <b>订单信息</b>
                    </br>
                    <p>订单编号: {0}</p>
                    <p>收件人：{1}</p>
                    <p>联系电话：{2} </p>
                    <p>收货地址：{3}</p>
                    </br>
                    </br>
                    </br>
                    {4}
                    </br>
                    </br>
                    </br>
                    <p>请打印附件快递单安排发货，谢谢。</p>\
                    </br>
                    <p>Brioni</p>""";
    public static final String PRODUCT_EMAIL_TEMPLATE =
            """
                    <p>商品信息</p>
                    <p>商品货号：{0}</p>
                    <p>商品名称：{1}</p>
                    <p>尺码：{2}</p>
                    <p>单价：{3}</p>
                    <p>颜色：{4}</p>
                    <p>数量：{5}</p>
                    <p>总价：{6}</p>
                    </br>
                    </br>
                    </br>
                    """;

    public static String convertEmailBodyInfo(OrderPO orderPO, List<OrderItemPO> orderItemPO) {

        StringBuilder productSb = new StringBuilder();
        orderItemPO.forEach(item -> {
            productSb.append(
                    MessageFormat.format(PRODUCT_EMAIL_TEMPLATE,
                            item.getSku(),
                            item.getProductName(),
                            item.getProductSize(),
                            item.getPrice(),
                            item.getColor(),
                            item.getQty(),
                            item.getPrice().multiply(BigDecimal.valueOf(item.getQty()))
                    )
            );
        });
        OrderAddressVO addressVO=   JsonUtil.convertToObject(StringUtils.firstNonBlank(  orderPO.getAddress(), ""), OrderAddressVO.class);
        return MessageFormat.format(ORDER_EMAIL_TEMPLATE,
                orderPO.getOrderNo(),
                addressVO.getUserName(),
                addressVO.getTelNumber(),
                addressVO.toAddressString(),
                productSb.toString()

        );
    }
}
