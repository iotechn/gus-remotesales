package com.dobbinsoft.gus.remotesales.aspect.log;

/**
 * LogRecord 日志记录持久层
 */
public interface LogRecordPersistent {

    /**
     * @param modelName 模块
     * @param content 日志正文
     * @param success 操作是否成功
     * @param logRefer Log引用,用于点击日志时跳转，此项目中暂时无用
     */
    void write(String modelName, String content, boolean success, LogRecordContext.LogRefer logRefer);

}
