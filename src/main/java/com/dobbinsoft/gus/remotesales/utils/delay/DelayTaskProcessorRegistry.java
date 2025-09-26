package com.dobbinsoft.gus.remotesales.utils.delay;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 延迟任务处理器注册表
 * 负责注册和查找任务处理器
 */
@Slf4j
@Component
public class DelayTaskProcessorRegistry {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<String, TaskHandlerInfo> handlerMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        registerHandlers();
    }
    
    /**
     * 注册所有任务处理器
     */
    private void registerHandlers() {
        Map<String, Object> beans = applicationContext.getBeansOfType(Object.class);
        
        for (Object bean : beans.values()) {
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                SchedulerTarget annotation = method.getAnnotation(SchedulerTarget.class);
                if (annotation != null) {
                    String taskName = annotation.name();
                    TaskHandlerInfo handlerInfo = new TaskHandlerInfo(bean, method);
                    handlerMap.put(taskName, handlerInfo);
                    log.info("注册任务处理器: taskName={}, class={}, method={}", 
                            taskName, bean.getClass().getSimpleName(), method.getName());
                }
            }
        }
    }
    
    /**
     * 获取任务处理器
     * 
     * @param taskName 任务名称
     * @return 处理器信息
     */
    public TaskHandlerInfo getHandler(String taskName) {
        return handlerMap.get(taskName);
    }
    
    /**
     * 任务处理器信息
     */
    @Getter
    public static class TaskHandlerInfo {
        private final Object bean;
        private final Method method;
        
        public TaskHandlerInfo(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
        }

    }
}
