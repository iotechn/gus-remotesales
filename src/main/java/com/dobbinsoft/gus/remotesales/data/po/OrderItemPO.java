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
@TableName("rs_order_item")
@Schema(description = "订单SKU表")
public class OrderItemPO extends SoftDeleteMyBatisBaseEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "商品编码")
    private String smc;

    @Schema(description = "商品SKU")
    private String sku;

    @Schema(description = "颜色编码")
    private String colorCode;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "二级分类编码")
    private String departmentCode;

    @Schema(description = "二级分类名称")
    private String departmentName;

    @Schema(description = "一级分类编码")
    private String departmentGroupCode;

    @Schema(description = "二级分类名称")
    private String departmentGroupName;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品描述")
    private String productDesc;

    @Schema(description = "商品图片URL")
    private String productPic;

    @Schema(description = "商品缩略图URL")
    private String productPicSmall;

    @Schema(description = "商品价格")
    private BigDecimal price;

    @Schema(description = "商品原价")
    private BigDecimal originalPrice;

    @Schema(description = "商品数量")
    private Integer qty;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "内部备注")
    private String innerRemark;

    @Schema(description = "是否支持7天内退货（1=支持，0=不支持）")
    private Boolean returnInSevenDays;

    @Schema(description = "商品尺码")
    private String productSize;

    @Schema(description = "markDown")
    private String markDown;
}
