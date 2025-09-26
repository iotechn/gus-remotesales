package com.dobbinsoft.gus.remotesales.data.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
public class ExpressNumberVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "交付方式")
    String deliveryMethod;
    @Schema(description = "提交时间")
    ZonedDateTime submitTime;

    /**
     * 配送地址JSON对象
     */
    @Schema(description = "配送地址")
    private String address;

    @Schema(description = "物流公司名称")
    private String logisticsCompany;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "发货时间")
    private ZonedDateTime deliveryTime;

    @Schema(description = "实际收货日期")
    private ZonedDateTime receiveTime;
    @Schema(description = "用户真实姓名")
    private String customerName;

    @Schema(description = "用户手机号码")
    private String customerMobile;

}
