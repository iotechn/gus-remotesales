package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class MpnewsArticle implements Serializable {
    private String title;
    @JsonProperty("thumb_media_id")
    private String thumbMediaId;
    private String author;
    @JsonProperty("content_source_url")
    private String contentSourceUrl;
    private String content;
    private String digest;
    /** @deprecated */
    @Deprecated
    @JsonProperty("show_cover_pic")
    private String showCoverPic;
}