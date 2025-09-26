package com.dobbinsoft.gus.remotesales.data.po.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Base entity class for soft delete functionality using MyBatis Plus.
 * Extends MyBatisBaseEntity and implements SoftDelete interface.
 * Provides fields for soft deletion status.
 * @param <K> Type of the entity's ID.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class SoftDeleteMyBatisBaseEntity<K extends Serializable> extends MyBatisBaseEntity<K>
        implements SoftDelete {

    @TableLogic(value = "false", delval = "true")
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
