package com.dobbinsoft.gus.remotesales.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dobbinsoft.gus.remotesales.data.IMapper;
import com.dobbinsoft.gus.remotesales.data.po.OrderRefundPO;
import com.dobbinsoft.gus.remotesales.data.vo.SystemRefundOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface OrderRefundMapper extends IMapper<OrderRefundPO> {
    IPage<SystemRefundOrderVO> getSystemRefundOrderList(Page<SystemRefundOrderVO> pageParams, @Param("params") Map<String, Object> params);
}
