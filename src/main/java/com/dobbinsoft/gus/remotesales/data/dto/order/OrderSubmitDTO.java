package com.dobbinsoft.gus.remotesales.data.dto.order;

import com.dobbinsoft.gus.remotesales.data.enums.OrderTypeEnum;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.web.exception.ServiceException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class OrderSubmitDTO {

    @Schema(description = "订单商品 订单时非空")
    private List<OrderItemSubmitDTO> items;

    @NotBlank
    @Schema(description = "外部联系人ID")
    private String externalUserId;

    @Schema(description = "订金 订金单时非空")
    private BigDecimal depositAmount;

    @Schema(description = "订单 定价单 备注")
    private String remark;

    @Schema(description = "订单 定价单 内部备注")
    private String innerRemark;

    @NotNull
    @Schema(description = "订单类型 1:订单 2:订金单")
    private Integer type;

    public void valid() {
        if (type == OrderTypeEnum.DEPOSIT_ORDER.getCode().intValue()) {
            // 如果是订金单，则必传订金
            if (depositAmount == null) {
                throw new ServiceException(RemotesalesErrorCode.PARAMERROR);
            }
        } else if (type == OrderTypeEnum.ORDER.getCode().intValue()) {
            // 如果是订单，则必传商品，订金字段置空
            if (items == null || items.isEmpty()) {
                throw new ServiceException(RemotesalesErrorCode.PARAMERROR);
            }
            depositAmount = null;
        } else {
            throw new ServiceException(RemotesalesErrorCode.PARAMERROR);
        }
    }

}
