package com.dobbinsoft.gus.remotesales.utils.delay;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 调度器目标注解
 * 用于标记延迟任务的处理方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerTarget {
    
    /**
     * 任务名称
     * 必须与创建任务时使用的taskName一致
     */
    String name();
    
    /**
     * 任务描述
     */
    String description() default "";
}
