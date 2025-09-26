package com.dobbinsoft.gus.remotesales.controller.wecom;

import com.dobbinsoft.gus.remotesales.data.vo.customer.CustomerBasicVO;
import com.dobbinsoft.gus.remotesales.service.CustomerService;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/wecom/customer")
@Tag(name = "企业微信客人接口")
public class WecomCustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/basic/{externalUserId}")
    @Operation(summary = "获取客人基础信息")
    public R<CustomerBasicVO> basic(@PathVariable("externalUserId") String externalUserId) {
        return R.success(customerService.getCustomer(externalUserId));
    }

}
