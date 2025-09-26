package com.dobbinsoft.gus.remotesales.client.gus.logistics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ExpressOrderCreateDTO {

    @Schema(description = "应用订单编号")
    private String orderNo;

    @Schema(description = "运单号")
    private String transNo;

    @Schema(description = "承运商编码")
    private LpCode lpCode;

    @Schema(description = "承运商名称")
    private String lpName;

    @Schema(description = "用于幂等校验")
    private String uniqueKey;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "发件人")
    private Sender sender;

    @Schema(description = "收件人")
    private Receiver receiver;


    @Getter
    @Setter
    public static class Sender {
        private String name;
        private String mobile;
        private String country;
        private String province;
        private String city;
        private String district;
        private String address;
        private String zipcode;
    }

    @Getter
    @Setter
    public static class Receiver {
        private String name;
        private String mobile;
        private String country;
        private String province;
        private String city;
        private String district;
        private String address;
        private String zipcode;
        private BigDecimal receiverLongitude;
        private BigDecimal receiverLatitude;
    }

}
