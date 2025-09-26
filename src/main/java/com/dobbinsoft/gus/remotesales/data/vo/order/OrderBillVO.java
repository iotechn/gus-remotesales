package com.dobbinsoft.gus.remotesales.data.vo.order;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OrderBillVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String type;
    private String title;
    private String taxNumber;
    private String companyAddress;
    private String telephone;
    private String bankName;
    private String bankAccount;

}
