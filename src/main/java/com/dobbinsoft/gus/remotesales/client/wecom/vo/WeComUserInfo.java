package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WeComUserInfo extends BaseWeComResponse {

    private String userid;
    private String name;
    private int[] department;
    private String position;
    private String mobile;
    private String gender;
    private String email;
    private String weixinid;
    @JsonProperty("avatar_mediaid")
    private String avatarMediaid;
    private String alias;
    private Integer status;
    private Integer enable;
    @JsonProperty("english_name")
    private String englishName;
    private String avatar;
    private ExtraAttrs extattr;
    private String[] departmentName;
    @JsonProperty("main_department")
    private Integer mainDepartment;
    private int[] order;
    @JsonProperty("is_leader_in_dept")
    private int[] isLeaderInDept;
    @JsonProperty("thumb_avatar")
    private String thumbAvatar;
    private String telephone;
    private String address;
    @JsonProperty("open_userid")
    private String openUserid;
    @JsonProperty("qr_code")
    private String qrCode;
    @JsonProperty("external_position")
    private String externalPosition;

}

