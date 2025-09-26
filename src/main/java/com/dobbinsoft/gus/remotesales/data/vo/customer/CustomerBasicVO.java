package com.dobbinsoft.gus.remotesales.data.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerBasicVO {

    private String externalContactId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像URL")
    private String avatar;

    @Schema(description = "用户性别 0-未知 1-男性 2-女性")
    private Integer gender;

    @Schema(description = "用户是否在 CDB 中")
    private Boolean inCDB;

//    @Schema(description = "CDB 搜索结果")
//    private List<CustomerSearchVO> customerSearchVOS;

}
