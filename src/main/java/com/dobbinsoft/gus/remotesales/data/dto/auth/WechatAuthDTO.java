package com.dobbinsoft.gus.remotesales.data.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WechatAuthDTO {

    @NotBlank(message = "oauth code can't be blank")
    private String code;

}
