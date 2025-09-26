package com.dobbinsoft.gus.remotesales.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gus.remotesales")
public class RemotesaleProperties {

    /**
     * aes key
     */
    private String aesKey;
    /**
     * token 签发机构
     */
    private String iss;
    /**
     * private key for sign jwt token.
     */
    private String privateKey;

    /**
     * token 过期时间
     */
    private Integer expiresIn;

    /**
     * refresh token过期时间，默认30天
     */
    private Integer refreshExpiresIn = 30 * 24 * 60 * 60;

}
