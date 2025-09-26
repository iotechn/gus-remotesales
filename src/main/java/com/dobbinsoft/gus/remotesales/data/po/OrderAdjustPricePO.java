package com.dobbinsoft.gus.remotesales.data.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dobbinsoft.gus.remotesales.data.po.base.SoftDeleteMyBatisBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@TableName("rs_order_adjust_price")
@Schema(description = "调价申请表")
public class OrderAdjustPricePO extends SoftDeleteMyBatisBaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "冗余订单ID")
    private Long orderId;

    @Schema(description = "订单项ID")
    private Long orderItemId;

    @Schema(description = "冗余店铺ID")
    private String storeId;

    @Schema(description = "期望调整后的价格")
    private BigDecimal price;

    @Schema(description = "冗余商品原始价格")
    private BigDecimal originalPrice;

    @Schema(description = "商品数量")
    private Integer qty;

    @Schema(description = "审核人账号")
    private String auditorWwid;

    @Schema(description = "审核人姓名")
    private String auditorName;

    /**
     * com.dobbinsoft.gus.remotesales.data.enums.OrderAdjustPriceStatusEnum
     */
    @Schema(description = "审核状态（0=待审核, 1=通过, 2=驳回）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "内部备注")
    private String innerRemark;

    @Schema(description = "驳回备注")
    private String rejectRemark;
}
