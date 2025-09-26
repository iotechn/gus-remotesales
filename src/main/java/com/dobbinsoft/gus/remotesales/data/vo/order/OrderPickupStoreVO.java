package com.dobbinsoft.gus.remotesales.data.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class OrderPickupStoreVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "门店ID")
    private String storeId;
    @Schema(description = "门店名称")
    private String name;
    @Schema(description = "门店编码")
    private String code;
    @Schema(description = "省份")
    private String province;
    @Schema(description = "城市")
    private String city;
    @Schema(description = "区/县")
    private String district;
    @Schema(description = "详细地址")
    private String address;
    @Schema(description = "联系电话")
    private String phone;
    @Schema(description = "联系邮箱")
    private String email;
    @Schema(description = "邮政编码")
    private String postCode;

}
