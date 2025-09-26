package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;

public interface ConfigCenterService {
    /**
     * 获取所有当前品牌配置的动态表单
     *
     * @return 品牌所有配置内容列表
     */
    ConfigContentVO getBrandAllConfigContent();
}