package com.dobbinsoft.gus.remotesales.service.impl;

import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.service.ConfigCenterService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConfigCenterServiceImpl implements ConfigCenterService {

    @Resource
    private ConfigCenterClient configCenterClient;


    @Override
    public ConfigContentVO getBrandAllConfigContent() {
        return configCenterClient.getBrandAllConfigContent();
    }
}
