package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.remotesales.data.dto.refund.RefundApplyDTO;
import com.dobbinsoft.gus.remotesales.data.dto.refund.RefundAuditDTO;

public interface RefundService {

    void applyRefund(RefundApplyDTO refundApplyDTO);

    void auditRefund(RefundAuditDTO refundAuditDTO);

}
