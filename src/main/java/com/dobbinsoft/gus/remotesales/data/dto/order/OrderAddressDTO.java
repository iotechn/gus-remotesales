package com.dobbinsoft.gus.remotesales.data.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单收货地址信息")
public class OrderAddressDTO {

    @Schema(description = "收货人姓名")
    private String userName;
    @Schema(description = "邮政编码")
    private String postalCode;
    @Schema(description = "省份名称")
    private String provinceName;
    @Schema(description = "城市名称")
    private String cityName;
    @Schema(description = "区/县名称")
    private String countyName;
    @Schema(description = "详细地址信息")
    private String detailInfo;
    @Schema(description = "国家编码")
    private String nationalCode;
    @Schema(description = "联系电话")
    private String telNumber;

}
