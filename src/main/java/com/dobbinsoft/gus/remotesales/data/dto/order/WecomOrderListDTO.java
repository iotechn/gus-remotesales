package com.dobbinsoft.gus.remotesales.data.dto.order;

import com.dobbinsoft.gus.common.model.dto.PageDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class WecomOrderListDTO extends PageDTO {

    @Schema(description = "关键字，支持订单号、CA员工姓名、CA员工WWID、客人姓名、客人昵称等模糊搜索")
    private String keyword;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "下单开始时间")
    private ZonedDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "下单结束时间")
    private ZonedDateTime endTime;

    @Schema(description = "1:订单 2:订金单")
    private List<Integer> orderTypeList;

    @Schema(description = "支付状态列表（0=未支付, 1=已支付）")
    private List<Integer> payStatusList;

    @Schema(description = "订单状态列表（0=待转发, 10=待付款, 20=重置物流中, 30=待发货, 40=待提货, 50=待收货, 60=已完成, 70=已过期, -1=未知）")
    private List<Integer> statusList;

    @Schema(description = "配送方式列表（0=物流发货, 1=客户自提）")
    private List<Integer> deliveryMethodList;

    @Schema(description = "外部联系人ID列表")
    private List<String> customerExternalUseridList;

    @Schema(description = "CA员工WWID列表")
    private List<String> caWwidList;
    @Schema(description = "0 无退款 1有退款 ")
    private List<Integer>  refundStatusList;


    @Schema(description = "排序字段")
    private Sort sort;

    @Schema(description = "是否升序")
    private Boolean asc;

    public enum Sort {
        CREATE_TIME,
        PAY_TIME,
        AMOUNT

    }

}
