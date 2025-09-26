package com.dobbinsoft.gus.remotesales.data.dto.order;

import com.dobbinsoft.gus.remotesales.data.enums.DeliveryMethodEnum;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.web.exception.ServiceException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Schema(description = "订单配送信息")
public class OrderChooseDeliveryDTO {

    @NotNull(message = "order id can't be null")
    @Schema(description = "订单ID")
    private Long orderId;
    /**
     * com.dobbinsoft.gus.remotesales.data.enums.DeliveryMethodEnum
     */
    @NotNull(message = "delivery method can't be null")
    @Schema(description = "配送方式（0=物流发货, 1=客户自提）")
    private Integer deliveryMethod;
    @Schema(description = "收货地址信息")
    private OrderAddressDTO address;
    @Schema(description = "选择提货门店ID")
    private String storeId;

    public void valid() {
        // 校验配送方式
        boolean validDelivery = false;
        for (DeliveryMethodEnum e : DeliveryMethodEnum.values()) {
            if (e.getCode().equals(deliveryMethod)) {
                validDelivery = true;
                break;
            }
        }
        if (!validDelivery) {
            throw new ServiceException(RemotesalesErrorCode.PARAMERROR, "delivery method is invalid");
        }
        // 物流发货校验地址
        if (DeliveryMethodEnum.LOGISTICS.getCode().equals(deliveryMethod)) {
            if (address == null
                    || StringUtils.isEmpty(address.getDetailInfo())
                    || StringUtils.isEmpty(address.getTelNumber())
                    || StringUtils.isEmpty(address.getUserName())) {
                throw new ServiceException(RemotesalesErrorCode.PARAMERROR, "address is invalid");
            }
        }
        // 自提校验门店ID
        if (DeliveryMethodEnum.SELF_PICKUP.getCode().equals(deliveryMethod)) {
            if (storeId == null) {
                throw new ServiceException(RemotesalesErrorCode.PARAMERROR, "pickup store id can't be null");
            }
        }
    }

}
