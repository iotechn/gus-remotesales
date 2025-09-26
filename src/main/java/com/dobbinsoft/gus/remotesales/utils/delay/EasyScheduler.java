package com.dobbinsoft.gus.remotesales.utils.delay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

/**
 * 简单调度器工具类
 * 提供静态方法创建延迟任务
 */
@Component
public class EasyScheduler {
    
    private static DelayTask delayTask;
    
    @Autowired
    public void setDelayTask(DelayTask delayTask) {
        EasyScheduler.delayTask = delayTask;
    }
    
    /**
     * 创建延迟任务
     * 
     * @param taskName 任务名称
     * @param executeTime 执行时间
     * @param parameters 任务参数
     * @return 任务ID
     */
    public static String createJob(String taskName, ZonedDateTime executeTime, Object[] parameters) {
        if (delayTask == null) {
            throw new IllegalStateException("DelayTask未初始化，请确保Spring容器已启动");
        }
        return delayTask.createJob(taskName, executeTime, parameters);
    }
    
    /**
     * 取消延迟任务
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    public static boolean cancelJob(String taskId) {
        if (delayTask == null) {
            throw new IllegalStateException("DelayTask未初始化，请确保Spring容器已启动");
        }
        return delayTask.cancelJob(taskId);
    }
}
