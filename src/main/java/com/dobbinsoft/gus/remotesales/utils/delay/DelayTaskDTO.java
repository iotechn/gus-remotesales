package com.dobbinsoft.gus.remotesales.utils.delay;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * 延迟任务数据传输对象
 */
@Data
public class DelayTaskDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务名称/标识
     */
    private String taskName;
    
    /**
     * 执行时间
     */
    private ZonedDateTime executeTime;
    
    /**
     * 任务参数
     */
    private Object[] parameters;
    
    /**
     * 任务类名
     */
    private String targetClass;
    
    /**
     * 任务方法名
     */
    private String targetMethod;
    
    /**
     * 创建时间
     */
    private ZonedDateTime createTime;
    
    /**
     * 重试次数
     */
    private Integer retryCount = 0;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;
    
    /**
     * 任务状态
     */
    private String status = "PENDING";
}
