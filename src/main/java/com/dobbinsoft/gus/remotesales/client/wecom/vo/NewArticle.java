package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class NewArticle implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String title;
    private String description;
    private String url;
    @JsonProperty("picurl")
    private String picUrl;
}