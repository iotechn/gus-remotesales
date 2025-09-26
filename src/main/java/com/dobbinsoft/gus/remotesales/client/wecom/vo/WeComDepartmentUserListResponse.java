package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class WeComDepartmentUserListResponse extends BaseWeComResponse {
    @JsonProperty("userlist")
    private List<DepartmentUser> userList;

    @Data
    public static class DepartmentUser {
        private String userid;
        private String name;
        private int[] department;
        @JsonProperty("open_userid")
        private String openUserid;
    }
} 