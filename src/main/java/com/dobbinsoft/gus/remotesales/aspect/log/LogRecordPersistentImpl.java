package com.dobbinsoft.gus.remotesales.aspect.log;

import com.dobbinsoft.gus.remotesales.data.dto.session.BoSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WechatSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.enums.OperationLogChannelEnum;
import com.dobbinsoft.gus.remotesales.data.po.OperationLogPO;
import com.dobbinsoft.gus.remotesales.mapper.OperationLogMapper;
import com.dobbinsoft.gus.remotesales.utils.RequestUtils;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.ZonedDateTime;

/**
 * 使用MySQL收集业务日志
 */
@Slf4j
@Component
public class LogRecordPersistentImpl implements LogRecordPersistent {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public void write(String modelName, String content, boolean success, LogRecordContext.LogRefer logRefer) {

        BoSessionInfoDTO boSession = SessionUtils.getBoSession();
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        WechatSessionInfoDTO wechatSession = SessionUtils.getWechatSession();

        String name;
        String wwId;
        OperationLogChannelEnum operationLogChannelEnum;
        if (boSession != null) {
            name = boSession.getEmployeeEmail();
            wwId = boSession.getEmployeeEmail();
            operationLogChannelEnum = OperationLogChannelEnum.WEB;
        } else if (wecomSession != null) {
            name = wecomSession.getUserId();
            wwId = wecomSession.getUserId();
            operationLogChannelEnum = OperationLogChannelEnum.WECOM;
        } else if (wechatSession != null) {
            name = wechatSession.getNickname();
            wwId = wechatSession.getUnionid();
            operationLogChannelEnum = OperationLogChannelEnum.WECHAT;
        } else {
            log.warn("[业务日志] 错配session");
            return;
        }
        try {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        saveLog(modelName, content, success, name, wwId, operationLogChannelEnum.getCode());
                    }
                });
            } else {
                saveLog(modelName, content, success, name, wwId, operationLogChannelEnum.getCode());
            }
        } catch (Exception e) {
            log.error("log write error:{},log:[modelName:{},content:{},success:{},name:{},wwId:{},operationChannel:{}]", e.getMessage(), modelName, content, success, name, wwId, operationLogChannelEnum.getCode());
        }

    }


    private void saveLog(String modelName, String content, boolean success, String name, String wwId, Integer operationChannel) {
        OperationLogPO adminOperationLogDO = new OperationLogPO();
        adminOperationLogDO.setWwid(wwId);
        adminOperationLogDO.setUserName(name);
        adminOperationLogDO.setComment(content);
        adminOperationLogDO.setChannel(operationChannel);
        adminOperationLogDO.setSuccess(success);
        adminOperationLogDO.setOperateTime(ZonedDateTime.now());
        adminOperationLogDO.setModelName(modelName);
        adminOperationLogDO.setIpAddress(RequestUtils.getClientIp());
        operationLogMapper.insert(adminOperationLogDO);
    }
}
