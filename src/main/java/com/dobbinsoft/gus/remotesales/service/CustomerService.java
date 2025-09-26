package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.remotesales.data.vo.customer.CustomerBasicVO;

public interface CustomerService {

    CustomerBasicVO getCustomer(String externalContactId);

}
