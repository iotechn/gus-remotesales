package com.dobbinsoft.gus.remotesales.service.impl;

import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.client.wecom.WeComAdapterClient;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.WeComExternalContactResponse;
import com.dobbinsoft.gus.remotesales.data.vo.customer.CustomerBasicVO;
import com.dobbinsoft.gus.remotesales.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private ConfigCenterClient configCenterClient;

    @Autowired
    private WeComAdapterClient weComAdapterClient;


    @Override
    public CustomerBasicVO getCustomer(String externalContactId) {
        ConfigContentVO brandAllConfigContent = configCenterClient.getBrandAllConfigContent();
        WeComExternalContactResponse externalContact = weComAdapterClient.getExternalContact(brandAllConfigContent.getBrand().getAgentId(), externalContactId);
        if (externalContact == null || externalContact.getExternalContact() == null) {
            return null;
        }
        CustomerBasicVO vo = new CustomerBasicVO();
        vo.setExternalContactId(externalContactId);
        vo.setGender(externalContact.getExternalContact().getGender());
        vo.setNickname(externalContact.getExternalContact().getName());
        vo.setAvatar(externalContact.getExternalContact().getAvatar());
        vo.setInCDB(Boolean.FALSE);
//        vo.setCustomerSearchVOS(latestCustomers);
        return vo;
    }
}
