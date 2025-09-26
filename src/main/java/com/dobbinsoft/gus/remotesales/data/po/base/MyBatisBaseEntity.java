package com.dobbinsoft.gus.remotesales.data.po.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Base entity class for MyBatis Plus entities.
 * Provides common fields such as ID, versioning, auditing fields, and tenant ID.
 * @param <K> Type of the entity's ID.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class MyBatisBaseEntity<K extends Serializable> implements BaseEntity<K> {

    @TableId(value = "id", type = IdType.AUTO)
    private K id;

    @Version
    @TableField(value = "version")
    private Long version = 1L;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private ZonedDateTime createdTime;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "modified_time", fill = FieldFill.INSERT_UPDATE)
    private ZonedDateTime modifiedTime;

    @TableField(value = "modified_by", fill = FieldFill.INSERT_UPDATE)
    private String modifiedBy;

    @TableField(value = "tenant_id")
    private String tenantId;
}
