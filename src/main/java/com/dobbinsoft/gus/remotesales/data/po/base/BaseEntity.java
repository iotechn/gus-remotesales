package com.dobbinsoft.gus.remotesales.data.po.base;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface BaseEntity<K extends Serializable> {
    K getId();

    Long getVersion();

    ZonedDateTime getCreatedTime();

    String getCreatedBy();

    ZonedDateTime getModifiedTime();

    String getModifiedBy();

    String getTenantId();
}
