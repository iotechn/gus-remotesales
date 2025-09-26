package com.dobbinsoft.gus.remotesales.client.gus.logistics.model;

import lombok.Getter;

/**
 * 快递公司编码枚举
 */
@Getter
public enum LpCode {
    
    /**
     * 京东
     */
    JD("京东", "JD Express"),
    
    /**
     * 京东快运
     */
    JDKY("京东快运", "JD Logistics"),
    
    /**
     * 德邦
     */
    DB("德邦", "Deppon"),
    
    /**
     * 顺丰
     */
    SF("顺丰", "SF Express"),
    
    /**
     * 极兔
     */
    JT("极兔", "J&T Express"),
    
    /**
     * 圆通
     */
    YT("圆通", "YTO Express"),
    
    /**
     * 申通
     */
    ST("申通", "STO Express"),
    
    /**
     * 中通
     */
    ZT("中通", "ZTO Express"),
    
    /**
     * 中通快运
     */
    ZTKY("中通快运", "ZTO Freight"),
    
    /**
     * 韵达
     */
    YD("韵达", "Yunda Express"),
    
    /**
     * EMS
     */
    EMS("EMS", "EMS"),
    
    /**
     * 跨越
     */
    KY("跨越", "Kuayue Express");
    
    private final String name;
    private final String englishName;
    
    LpCode(String name, String englishName) {
        this.name = name;
        this.englishName = englishName;
    }

}
