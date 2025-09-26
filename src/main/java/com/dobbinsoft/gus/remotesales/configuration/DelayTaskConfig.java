package com.dobbinsoft.gus.remotesales.configuration;

import com.dobbinsoft.gus.remotesales.utils.delay.DelayTaskExecutor;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * 延迟任务配置类
 * 负责启动和停止延迟任务执行器
 */
@Slf4j
@Configuration
public class DelayTaskConfig implements ApplicationRunner {
    
    @Autowired
    private DelayTaskExecutor delayTaskExecutor;
    
    @Override
    public void run(ApplicationArguments args) {
        // 应用启动后启动延迟任务执行器
        delayTaskExecutor.start();
        log.info("延迟任务执行器配置完成");
    }
    
    @PreDestroy
    public void destroy() {
        // 应用关闭时停止延迟任务执行器
        delayTaskExecutor.stop();
        log.info("延迟任务执行器已停止");
    }
}
