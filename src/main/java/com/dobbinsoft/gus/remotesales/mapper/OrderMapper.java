package com.dobbinsoft.gus.remotesales.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dobbinsoft.gus.remotesales.data.IMapper;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import com.dobbinsoft.gus.remotesales.data.vo.customer.OrderCustomerVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.OrderItemExportVO;
import com.dobbinsoft.gus.remotesales.data.vo.report.StorePerformanceVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper extends IMapper<OrderPO> {

    List<OrderCustomerVo> selectOrderCustomer(@Param(Constants.WRAPPER) LambdaQueryWrapper<OrderPO> queryWrapper);

    IPage<OrderCustomerVo> selectOrderCustomer(Page<OrderCustomerVo> pageParams, @Param(Constants.WRAPPER) LambdaQueryWrapper<OrderPO> queryWrapper);

    StorePerformanceVo selectOrderPerformance(@Param("storeId") String storeId, @Param("regionId") String regionId);

    Page<OrderItemExportVO> selectOrderAndItemByPage(Page<OrderPO> page, @Param(Constants.WRAPPER) LambdaQueryWrapper<OrderPO> wrapper);
}
