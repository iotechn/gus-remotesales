package com.dobbinsoft.gus.remotesales.data.vo.report;

import com.dobbinsoft.gus.remotesales.data.po.OrderItemPO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderItemExportVO extends OrderItemPO {


    private String orderNo;
    private String caWwid;
    private String caName;
    private String storeName;
    private String customerNickname;
    private Integer status;
    private BigDecimal amount;
    private BigDecimal payAmount;
    private Integer deliveryMethod;
    private String logisticsCompany;
    private String logisticsNo;
    private String posNumber;
    private String receiptNumber;
    private String xStoreId;
}
