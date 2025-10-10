package com.dobbinsoft.gus.remotesales;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(scanBasePackages = "com.dobbinsoft.gus")
@MapperScan("com.dobbinsoft.gus.remotesales.mapper")
public class GusRemotesalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GusRemotesalesApplication.class, args);
    }

}
