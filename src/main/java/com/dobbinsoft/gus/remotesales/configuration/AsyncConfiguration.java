package com.dobbinsoft.gus.remotesales.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        return Executors.newFixedThreadPool(8);
    }

}