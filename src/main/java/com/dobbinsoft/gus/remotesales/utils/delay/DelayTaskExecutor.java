package com.dobbinsoft.gus.remotesales.utils.delay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 延迟任务执行器
 * 负责从Redis延迟队列中取出到期的任务并执行
 */
@Slf4j
@Component
public class DelayTaskExecutor {
    
    private static final String DELAY_QUEUE_KEY = "delay:task:queue";
    private static final String TASK_PROCESSING_KEY = "delay:task:processing:";
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    
    @Autowired
    private DelayTask delayTask;
    
    @Autowired
    private DelayTaskProcessorRegistry processorRegistry;
    
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    
    /**
     * 启动延迟任务执行器
     */
    public void start() {
        executorService.submit(this::processDelayTasks);
        log.info("延迟任务执行器已启动");
    }
    
    /**
     * 停止延迟任务执行器
     */
    public void stop() {
        executorService.shutdown();
        log.info("延迟任务执行器已停止");
    }
    
    /**
     * 处理延迟任务的主循环
     */
    private void processDelayTasks() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long currentTime = System.currentTimeMillis();
                
                // 从Redis ZSet中获取到期的任务
                Set<String> expiredTasks = redisTemplate.opsForZSet().rangeByScore(DELAY_QUEUE_KEY, 0, currentTime);
                
                if (expiredTasks != null && !expiredTasks.isEmpty()) {
                    for (String taskId : expiredTasks) {
                        // 使用Redis原子操作确保任务只被一个实例处理
                        if (tryLockTask(taskId)) {
                            executorService.submit(() -> executeTask(taskId));
                        }
                    }
                }
                
                // 休眠1秒后继续检查
                Thread.sleep(1000);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("延迟任务执行器被中断");
                break;
            } catch (Exception e) {
                log.error("处理延迟任务时发生错误", e);
            }
        }
    }
    
    /**
     * 尝试锁定任务，确保任务只被一个实例处理
     * 
     * @param taskId 任务ID
     * @return 是否成功锁定
     */
    private boolean tryLockTask(String taskId) {
        String lockKey = TASK_PROCESSING_KEY + taskId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "1");
        if (Boolean.TRUE.equals(success)) {
            // 设置锁过期时间为5分钟
            redisTemplate.expire(lockKey, java.time.Duration.ofMinutes(5));
            return true;
        }
        return false;
    }
    
    /**
     * 执行具体的延迟任务
     * 
     * @param taskId 任务ID
     */
    private void executeTask(String taskId) {
        try {
            // 从Redis中获取任务数据
            DelayTaskDTO task = delayTask.getTask(taskId);
            if (task == null) {
                log.warn("任务不存在: taskId={}", taskId);
                return;
            }
            
            // 更新任务状态为执行中
            delayTask.updateTaskStatus(taskId, "EXECUTING");
            
            // 从延迟队列中移除任务
            redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, taskId);
            
            // 执行任务
            executeTaskMethod(task);
            
            // 更新任务状态为已完成
            delayTask.updateTaskStatus(taskId, "COMPLETED");
            
            log.info("延迟任务执行完成: taskId={}, taskName={}", taskId, task.getTaskName());
            
        } catch (Exception e) {
            log.error("执行延迟任务失败: taskId={}", taskId, e);
            
            // 处理重试逻辑
            handleTaskRetry(taskId, e);
            
        } finally {
            // 释放任务锁
            String lockKey = TASK_PROCESSING_KEY + taskId;
            redisTemplate.delete(lockKey);
        }
    }
    
    /**
     * 执行任务方法
     * 
     * @param task 任务信息
     */
    private void executeTaskMethod(DelayTaskDTO task) throws Exception {
        // 根据任务名称查找对应的处理器
        DelayTaskProcessorRegistry.TaskHandlerInfo handlerInfo = processorRegistry.getHandler(task.getTaskName());
        if (handlerInfo == null) {
            throw new RuntimeException("未找到任务处理器: " + task.getTaskName());
        }
        
        // 执行方法
        handlerInfo.getMethod().invoke(handlerInfo.getBean(), task.getParameters());
    }
    
    /**
     * 处理任务重试
     * 
     * @param taskId 任务ID
     * @param e 异常
     */
    private void handleTaskRetry(String taskId, Exception e) {
        try {
            DelayTaskDTO task = delayTask.getTask(taskId);
            if (task != null) {
                int retryCount = task.getRetryCount() + 1;
                task.setRetryCount(retryCount);
                
                if (retryCount <= task.getMaxRetryCount()) {
                    // 重新加入延迟队列，延迟5分钟后重试
                    ZonedDateTime retryTime = ZonedDateTime.now().plusMinutes(5);
                    long retryTimestamp = retryTime.toInstant().toEpochMilli();
                    redisTemplate.opsForZSet().add(DELAY_QUEUE_KEY, taskId, retryTimestamp);
                    
                    delayTask.updateTaskStatus(taskId, "RETRYING");
                    log.info("任务将重试: taskId={}, retryCount={}", taskId, retryCount);
                } else {
                    delayTask.updateTaskStatus(taskId, "FAILED");
                    log.error("任务重试次数已达上限: taskId={}, maxRetryCount={}", taskId, task.getMaxRetryCount());
                }
            }
        } catch (Exception ex) {
            log.error("处理任务重试失败: taskId={}", taskId, ex);
        }
    }
}
