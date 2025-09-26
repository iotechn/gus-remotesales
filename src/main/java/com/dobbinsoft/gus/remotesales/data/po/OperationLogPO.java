package com.dobbinsoft.gus.remotesales.data.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dobbinsoft.gus.remotesales.data.po.base.SoftDeleteMyBatisBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
@TableName("rs_operation_log")
@Schema(description = "操作日志")
public class OperationLogPO extends SoftDeleteMyBatisBaseEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模块名称")
    private String modelName;
    @Schema(description = "操作行为（如创建、删除、更新等）")
    private String action;

    @Schema(description = "WWID（用户唯一标识）")
    private String wwid;

    @Schema(description = "执行操作的用户名")
    private String userName;

    @Schema(description = "操作备注（额外信息）")
    private String comment;

    @Schema(description = "操作的IP地址")
    private String ipAddress;

    @Schema(description = "操作端 1-企业微信 2-WEB")
    private Integer channel;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "操作时间")
    private ZonedDateTime operateTime;

}
