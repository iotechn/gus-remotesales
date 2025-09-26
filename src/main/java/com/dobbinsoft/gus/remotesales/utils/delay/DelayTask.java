package com.dobbinsoft.gus.remotesales.utils.delay;

import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 延迟任务调度器
 * 基于Redis ZSet实现的延迟队列
 */
@Slf4j
@Component
public class DelayTask {
    
    private static final String DELAY_QUEUE_KEY = "delay:task:queue";
    private static final String TASK_DATA_PREFIX = "delay:task:data:";
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    
    /**
     * 创建延迟任务
     * 
     * @param taskName 任务名称
     * @param executeTime 执行时间
     * @param parameters 任务参数
     * @return 任务ID
     */
    public String createJob(String taskName, ZonedDateTime executeTime, Object[] parameters) {
        String taskId = UUID.randomUUID().toString();

        DelayTaskDTO taskDTO = new DelayTaskDTO();
        taskDTO.setTaskId(taskId);
        taskDTO.setTaskName(taskName);
        taskDTO.setExecuteTime(executeTime);
        taskDTO.setParameters(parameters);
        taskDTO.setCreateTime(ZonedDateTime.now());

        // 将任务数据存储到Redis
        String taskDataKey = TASK_DATA_PREFIX + taskId;
        String taskDataJson = JsonUtil.convertToString(taskDTO);
        redisTemplate.opsForValue().set(taskDataKey, taskDataJson);

        // 将任务ID添加到延迟队列，使用执行时间戳作为分数
        long executeTimestamp = executeTime.toInstant().toEpochMilli();
        redisTemplate.opsForZSet().add(DELAY_QUEUE_KEY, taskId, executeTimestamp);

        log.info("延迟任务创建成功: taskId={}, taskName={}, executeTime={}", taskId, taskName, executeTime);
        return taskId;
    }
    
    /**
     * 取消延迟任务
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    public boolean cancelJob(String taskId) {
        try {
            // 从延迟队列中移除
            redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, taskId);
            
            // 删除任务数据
            String taskDataKey = TASK_DATA_PREFIX + taskId;
            redisTemplate.delete(taskDataKey);
            
            log.info("延迟任务取消成功: taskId={}", taskId);
            return true;
            
        } catch (Exception e) {
            log.error("取消延迟任务失败: taskId={}", taskId, e);
            return false;
        }
    }
    
    /**
     * 获取任务信息
     * 
     * @param taskId 任务ID
     * @return 任务信息
     */
    public DelayTaskDTO getTask(String taskId) {
        String taskDataKey = TASK_DATA_PREFIX + taskId;
        String taskDataJson = redisTemplate.opsForValue().get(taskDataKey);

        if (taskDataJson != null) {
            return JsonUtil.convertToObject(taskDataJson, DelayTaskDTO.class);
        }
        return null;
    }
    
    /**
     * 更新任务状态
     * 
     * @param taskId 任务ID
     * @param status 新状态
     */
    public void updateTaskStatus(String taskId, String status) {
        DelayTaskDTO task = getTask(taskId);
        if (task != null) {
            task.setStatus(status);
            String taskDataKey = TASK_DATA_PREFIX + taskId;
            String taskDataJson = JsonUtil.convertToString(task);
            redisTemplate.opsForValue().set(taskDataKey, taskDataJson);
        }
    }
}
