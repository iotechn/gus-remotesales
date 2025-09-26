package com.dobbinsoft.gus.remotesales.data.dto.session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class WecomSessionInfoDTO {

    private String userId;

    private String name;

    private String currentStoreId; // 默认为第一个店铺。后期可扩展店铺切换功能。

    @Schema(description = "CA员工职位")
    private String caPosition;

    @Schema(description = "CA员工头像URL")
    private String caAvatar;

    @Schema(description = "position作为Store Manger时，管理的店铺列表")
    private List<Store> stores;

    public Store getCurrentStore() {
        return stores.stream().filter(store -> store.getStoreId().equals(currentStoreId)).findFirst().orElse(new Store());
    }

    public String getRegionId() {
        return getCurrentStore().getRegionId();
    }
    public String getRegionName() {
        return getCurrentStore().getRegionName();
    }
    public String getStoreId() {
        return getCurrentStore().getStoreId();
    }

    public String getStoreCode() {
        return getCurrentStore().getStoreCode();
    }

    public String getStoreName() {
        return getCurrentStore().getStoreName();
    }

    @Data
    public static class Store {
        private String storeId;
        private String storeCode;
        private String storeName;
        private String regionId;
        private String regionName;
        private Boolean isManager;
        private String managerEmail;
        private String managerWwid;
        private String managerName;
        private Integer caNumber;
        private String mobile;
        private String telephone;
        private String country;
        private String province;
        private String city;
        private String district;
        private String address;
        private String zipcode;
        private int[] wecomDeptIds;

    }

}
