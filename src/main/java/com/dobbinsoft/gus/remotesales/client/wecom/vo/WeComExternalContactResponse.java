package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class WeComExternalContactResponse extends BaseWeComResponse {
    @JsonProperty("external_contact")
    private ExternalContact externalContact;
    @JsonProperty("follow_user")
    private List<FollowUser> followUser;

    @Data
    public static class ExternalContact {
        @JsonProperty("external_userid")
        private String externalUserid;
        private String name;
        private String position;
        private String avatar;
        @JsonProperty("corp_name")
        private String corpName;
        @JsonProperty("corp_full_name")
        private String corpFullName;
        private Integer type;
        private Integer gender;
        private String unionid;
        @JsonProperty("is_subscribe")
        private Integer isSubscribe;
        @JsonProperty("subscriber_info")
        private SubscriberInfo subscriberInfo;
        @JsonProperty("external_profile")
        private ExternalProfile externalProfile;
    }

    @Data
    public static class SubscriberInfo {
        @JsonProperty("tag_id")
        private List<String> tagId;
        @JsonProperty("remark_mobiles")
        private List<String> remarkMobiles;
        private String remark;
    }

    @Data
    public static class ExternalProfile {
        @JsonProperty("external_attr")
        private List<ExternalAttr> externalAttr;
    }

    @Data
    public static class ExternalAttr {
        private Integer type;
        private String name;
        private Text text;
        private Web web;
        private MiniProgram miniprogram;

        @Data
        public static class Text {
            private String value;
        }
        @Data
        public static class Web {
            private String url;
            private String title;
        }
        @Data
        public static class MiniProgram {
            private String appid;
            private String pagepath;
            private String title;
        }
    }

    @Data
    public static class FollowUser {
        private String userid;
        private String remark;
        private String description;
        private Long createtime;
        private List<Tag> tags;
        @JsonProperty("remark_corp_name")
        private String remarkCorpName;
        @JsonProperty("remark_mobiles")
        private List<String> remarkMobiles;
        private String state;

        @Data
        public static class Tag {
            @JsonProperty("group_name")
            private String groupName;
            @JsonProperty("tag_name")
            private String tagName;
            private Integer type;
        }
    }
} 