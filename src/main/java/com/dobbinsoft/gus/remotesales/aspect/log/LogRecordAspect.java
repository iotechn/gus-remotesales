package com.dobbinsoft.gus.remotesales.aspect.log;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Component
@Aspect
public class LogRecordAspect {

    @Autowired
    private LogRecordPersistent logRecordPersistent;

    @Pointcut("@annotation(com.dobbinsoft.gus.remotesales.aspect.log.LogRecord)")
    public void logPointCut() {}


    @Around("logPointCut()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogRecord logRecord = method.getAnnotation(LogRecord.class);

        ExpressionParser parser = new SpelExpressionParser();
        StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();
        String[] params = discoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        EvaluationContext context = new StandardEvaluationContext();

        if (params != null) {
            for (int len = 0; len < params.length; len++) {
                context.setVariable(params[len], args[len]);
            }
        }

        try {
            Object proceed = joinPoint.proceed();
            saveLogWithoutException(context, proceed, parser, logRecord);
            return proceed;
        } finally {
            LogRecordContext.clear();
        }
    }

    /**
     * 保存日志，不抛出异常
     * @param context
     * @param proceed
     * @param parser
     * @param logRecord
     */
    private void saveLogWithoutException(EvaluationContext context, Object proceed, ExpressionParser parser, LogRecord logRecord) {
        try {
            Map<String, Object> contextMap = LogRecordContext.get();
            if (CollectionUtils.isNotEmpty(contextMap)) {
                contextMap.forEach(context::setVariable);
            }
            context.setVariable("return", proceed);
            String value = parser.parseExpression(logRecord.value(), new TemplateParserContext()).getValue(context, String.class);
            log.info("[业务日志] success={}", value);
            LogRecordContext.LogRefer refer = LogRecordContext.getRefer();
            logRecordPersistent.write(logRecord.modelName(),value, true, refer);
        } catch (Exception e) {
            Map<String, Object> customContext = LogRecordContext.get();
            log.error("[业务日志] 异常上下文： CustomContext={}", (customContext == null ? "{}" : JsonUtil.convertToString(customContext)));
            log.error("[业务日志] 异常", e);
        }
    }

}
