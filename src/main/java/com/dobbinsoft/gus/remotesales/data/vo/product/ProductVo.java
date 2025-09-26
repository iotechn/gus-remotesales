package com.dobbinsoft.gus.remotesales.data.vo.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVo {
    private String smc;
    private String sku;
    private String materialDesc;
    private String description;
    private BigDecimal price;
    private String pic;
    private String productName;
    private String color;
    private String colorCode;
    private BigDecimal originalPrice;
    private String departmentCode;
    private String departmentName;
    private String departmentGroupCode;
    private String departmentGroupName;
    private String size;
    private Integer stock;
    private String markDown;
}
