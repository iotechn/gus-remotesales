package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TaskCardButton implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String key;
    private String name;
    @JsonProperty("replace_name")
    private String replaceName;
    private String color;
    @JsonProperty("is_bold")
    private Boolean bold;
}
