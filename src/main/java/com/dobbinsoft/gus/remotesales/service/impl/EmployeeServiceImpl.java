package com.dobbinsoft.gus.remotesales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.client.gus.location.LocationFeignClient;
import com.dobbinsoft.gus.remotesales.client.gus.location.model.LocationVO;
import com.dobbinsoft.gus.remotesales.client.wecom.WeComAdapterClient;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.WeComDepartmentUserListResponse;
import com.dobbinsoft.gus.remotesales.data.constant.RoleTypeConstants;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.enums.OrderStatusEnum;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import com.dobbinsoft.gus.remotesales.data.vo.emp.EmployeeReportVO;
import com.dobbinsoft.gus.remotesales.data.vo.report.SalesPerformanceAllVo;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.mapper.OrderMapper;
import com.dobbinsoft.gus.remotesales.service.EmployeeService;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import com.dobbinsoft.gus.web.exception.ServiceException;
import com.dobbinsoft.gus.web.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    WeComAdapterClient weComAdapterClient;
    @Autowired
    LocationFeignClient locationFeignClient;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    ConfigCenterClient configCenterClient;

    @Override
    public List<EmployeeReportVO> findEmpOrderReport(String storeId,String customerExternalUserid) {
        if(StringUtils.isEmpty(storeId) && StringUtils.isEmpty(customerExternalUserid)) {
            throw  new ServiceException(RemotesalesErrorCode.PARAMERROR);
        }
        List<EmployeeReportVO> result = new ArrayList<>();
        Set<String> userId = new HashSet<>();
        if(StringUtils.isNotEmpty(storeId)) {
            ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
            String agentId = configContentVO.getBrand().getAgentId();
            R<LocationVO> storeResponse = locationFeignClient.detail(storeId);
            String deptIdsStr = storeResponse.getData().getWecomDeptId();
            if (StringUtils.isEmpty(deptIdsStr)) {
                log.warn("query storeHUb deptId is empty ,id:{}", storeId);
                return List.of();
            }

            List<String> deptIds = Arrays.asList(deptIdsStr.split(","));
            deptIds.forEach(deptId -> {
                WeComDepartmentUserListResponse response = weComAdapterClient.getDepartmentUser(agentId, deptId);
                List<WeComDepartmentUserListResponse.DepartmentUser> userList = response.getUserList();
                if (CollectionUtils.isEmpty(userList)) {
                    return;
                }

                result.addAll(userList.stream().map(v -> {
                    EmployeeReportVO vo = new EmployeeReportVO();
                    vo.setCaName(v.getName());
                    vo.setCawwId(v.getUserid());
                    userId.add(v.getUserid().trim());
                    return vo;
                }).toList());

            });
        }
        Map<String ,String> caMaps=new HashMap<>();
        if(StringUtils.isNotEmpty(customerExternalUserid)) {
            List<OrderPO> orderList=orderMapper.selectList(new LambdaQueryWrapper<OrderPO>().eq(OrderPO::getCustomerExternalUserid, customerExternalUserid));
            orderList.forEach(order -> {
                caMaps.put(order.getCaWwid(),order.getCaName());
                userId.add(order.getCaWwid());
            });
            userId.forEach(v->{
                EmployeeReportVO vo = new EmployeeReportVO();
                vo.setCaName(caMaps.get(v));
                vo.setCawwId(v);
                result.add(vo);
            });

        }
        Map<String, SalesPerformanceAllVo> reportData = getReportDataVoByCa(storeId,userId);
        result.forEach(vo -> {
            SalesPerformanceAllVo performanceData = reportData.get(vo.getCawwId());
            if (performanceData != null) {
                vo.setSalesOrderCount(performanceData.getOrderCount().intValue());
                vo.setSalesAmount(performanceData.getSalesAmount());
                vo.setLastSalesTime(performanceData.getLastSalesDate());
            }
        });

        return result;
    }

    private Map<String, SalesPerformanceAllVo> getReportDataVoByCa(String storeId, Set<String> userIds) {
        WecomSessionInfoDTO sessionInfoDTO= SessionUtils.getWecomSession();

        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<OrderPO>()
                .in(CollectionUtils.isNotEmpty(userIds), OrderPO::getCaWwid, userIds)
                .eq( !RoleTypeConstants.AREA_MANAGER.equals(sessionInfoDTO.getCaPosition())&&StringUtils.isNotEmpty(sessionInfoDTO.getCurrentStoreId()),OrderPO::getStoreId,sessionInfoDTO.getCurrentStoreId())
                .eq(StringUtils.isNotEmpty(storeId),OrderPO::getStoreId,storeId)
                .notIn(OrderPO::getStatus, List.of(
                        OrderStatusEnum.TO_FORWARD.getCode(),
                        OrderStatusEnum.TO_PAY.getCode(),
                        OrderStatusEnum.EXPIRED.getCode()
                ));
        // 1. 查询订单列表
        List<OrderPO> orders = orderMapper.selectList(wrapper);
        if (orders.isEmpty()) {
            return Map.of();
        }
        Map<String, List<OrderPO>> ordersByCa = orders.stream().collect(Collectors.groupingBy(OrderPO::getCaWwid));

        Map<String, SalesPerformanceAllVo> result = new HashMap<>();

        ordersByCa.forEach((caWwid, caOrders) -> {
            SalesPerformanceAllVo reportVO = new SalesPerformanceAllVo();
            //统计  订单量
            reportVO.setOrderCount(BigDecimal.valueOf(caOrders.size()));
            //统计销售额
            BigDecimal totalAmount = caOrders.stream()
                    .map(OrderPO::getAmount)
                    .reduce(new BigDecimal("0.0"), BigDecimal::add);
            reportVO.setSalesAmount(totalAmount);

            //统计最后销售日期
            reportVO.setLastSalesDate(caOrders.stream().filter(order-> Objects.nonNull(order.getCreatedTime())&&order.getCreatedTime().toInstant().toEpochMilli()!=0)
                    .map(order -> order.getCreatedTime().toLocalDateTime().atZone(ZoneId.systemDefault()))
                    .max(ZonedDateTime::compareTo)
                    .orElse(null));

            result.put(caWwid, reportVO);
        });

        return result;
    }
}
