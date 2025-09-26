package com.dobbinsoft.gus.remotesales.data.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class OrderCustomerVo {
    @Schema(description = "用户的外部联系人ID")
    private String customerExternalUserid;
    @Schema(description = "用户的性别")
    private Integer customerGender;

    @Schema(description = "用户昵称")
    private String customerNickname;



    @Schema(description = "用户头像URL")
    private String customerAvatar;

    @Schema(description = "最后购买时间")
    private ZonedDateTime lastOrderTime;
    @Schema(description = "总消费金额")
    private BigDecimal totalPrice;
    @Schema(description = "最后购买销售顾问名称")
    private String lastSalesCaName;

}
