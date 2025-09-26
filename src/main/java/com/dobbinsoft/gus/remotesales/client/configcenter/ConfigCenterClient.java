package com.dobbinsoft.gus.remotesales.client.configcenter;


import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;

public interface ConfigCenterClient {

    void save(ConfigContentVO configContentVO);

    ConfigContentVO getBrandAllConfigContent();

}
