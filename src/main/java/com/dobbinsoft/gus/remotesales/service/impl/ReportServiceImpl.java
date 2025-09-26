package com.dobbinsoft.gus.remotesales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.data.constant.RoleTypeConstants;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.enums.OrderStatusEnum;
import com.dobbinsoft.gus.remotesales.data.po.OrderItemPO;
import com.dobbinsoft.gus.remotesales.data.po.OrderPO;
import com.dobbinsoft.gus.remotesales.data.vo.customer.OrderCustomerVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.SalesPerformanceByKeyWordsVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.SalesPerformanceVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.StorePerformanceVo;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.mapper.OrderItemMapper;
import com.dobbinsoft.gus.remotesales.mapper.OrderMapper;
import com.dobbinsoft.gus.remotesales.service.ReportService;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import com.dobbinsoft.gus.web.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    /**
     * 时间范围常量
     */
    private static final String TIME_RANGE_TODAY = "today";
    private static final String TIME_RANGE_YESTERDAY = "yesterday";
    private static final String TIME_RANGE_THIS_WEEK = "thisWeek";
    private static final String TIME_RANGE_THIS_MONTH = "thisMonth";
    private static final String TIME_RANGE_THIS_YEAR = "thisYear";
    private static final String TIME_RANGE_LAST_MONTH = "lastMonth";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    private static void checkStoreAccess(String storeId) {
        if (!SessionUtils.getWecomSession().getStores().stream().map(WecomSessionInfoDTO.Store::getStoreId).toList().contains(storeId)) {
            throw new ServiceException(RemotesalesErrorCode.STORE_ACCESS_DENIED);
        }
    }


    @Override
    public Map<String, SalesPerformanceVo> getSmStoreReport(String storeId) {
        checkStoreAccess(storeId);

        Map<String, SalesPerformanceVo> result = new HashMap<>();

        // 获取时间范围
        LocalDateTime now = LocalDateTime.now();
        // 今日：今天0点到当前时间
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        // 昨日：昨天0点到昨天23:59:59
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayStart.minusSeconds(1);
        // 本周：本周一0点到当前时间
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        // 本月：本月1号0点到当前时间
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        // 本年：本年1月1号0点到当前时间
        LocalDateTime yearStart = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay();

        // 构建基础查询条件
        LambdaQueryWrapper<OrderPO> baseWrapper = new LambdaQueryWrapper<OrderPO>()
                .eq(StringUtils.isNotBlank(storeId), OrderPO::getStoreId, storeId)
                .notIn(OrderPO::getStatus, List.of(
                        OrderStatusEnum.TO_FORWARD.getCode(),
                        OrderStatusEnum.TO_PAY.getCode(),
                        OrderStatusEnum.EXPIRED.getCode()
                ));

        // 1. 今日数据：今天0点到当前时间
        LambdaQueryWrapper<OrderPO> todayWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_TODAY, getReportDataVo(todayWrapper));

        // 2. 昨日数据：昨天0点到昨天23:59:59
        LambdaQueryWrapper<OrderPO> yesterdayWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(yesterdayStart.atZone(ZoneId.systemDefault()).toInstant()))
                .le(OrderPO::getCreatedTime, Date.from(yesterdayEnd.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_YESTERDAY, getReportDataVo(yesterdayWrapper));

        // 3. 本周数据：本周一0点到当前时间
        LambdaQueryWrapper<OrderPO> weekWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(weekStart.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_THIS_WEEK, getReportDataVo(weekWrapper));

        // 4. 本月数据：本月1号0点到当前时间
        LambdaQueryWrapper<OrderPO> monthWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(monthStart.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_THIS_MONTH, getReportDataVo(monthWrapper));

        // 5. 本年数据：本年1月1号0点到当前时间
        LambdaQueryWrapper<OrderPO> yearWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(yearStart.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_THIS_YEAR, getReportDataVo(yearWrapper));

        return result;
    }

    @Override
    public Map<String, SalesPerformanceVo> trendReportByDay(String storeId, String regionId, Integer dayRange) {
        if (Objects.nonNull(storeId)) {
            checkStoreAccess(storeId);
        }

        // 1. 获取时间范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(dayRange);

        // 2. 查询订单数据
        List<OrderPO> orders = orderMapper.selectList(new LambdaQueryWrapper<OrderPO>()
                .eq(StringUtils.isNotBlank(storeId), OrderPO::getStoreId, storeId)
                .eq(StringUtils.isNotBlank(regionId), OrderPO::getRegionId, regionId)
                .notIn(OrderPO::getStatus, List.of(
                        OrderStatusEnum.TO_FORWARD.getCode(),
                        OrderStatusEnum.TO_PAY.getCode(),
                        OrderStatusEnum.EXPIRED.getCode()
                ))
                .ge(OrderPO::getCreatedTime, Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()))
                .le(OrderPO::getCreatedTime, Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant())));

        if (orders.isEmpty()) {
            return Map.of();
        }

        // 3. 查询订单明细
        List<OrderItemPO> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemPO>()
                        .in(OrderItemPO::getOrderId, orders.stream().map(OrderPO::getId).toList())
        );

        // 4. 按日期分组统计
        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                dayOrders -> {
                                    SalesPerformanceVo reportVO = new SalesPerformanceVo();

                                    // 4.1 统计订单量
                                    reportVO.setOrderCount(BigDecimal.valueOf(dayOrders.size()));

                                    // 4.2 统计销售额
                                    BigDecimal totalAmount = dayOrders.stream()
                                            .map(OrderPO::getAmount)
                                            .reduce(new BigDecimal("0.0"), BigDecimal::add);
                                    reportVO.setSalesAmount(totalAmount);

                                    // 4.3 统计销售量
                                    List<Long> orderIds = dayOrders.stream().map(OrderPO::getId).toList();
                                    long salesCount = orderItems.stream()
                                            .filter(item -> orderIds.contains(item.getOrderId()))
                                            .mapToLong(OrderItemPO::getQty)
                                            .sum();
                                    reportVO.setSalesCount(BigDecimal.valueOf(salesCount));

                                    return reportVO;
                                }
                        )
                ));
    }

    @Override
    public Map<String, List<SalesPerformanceByKeyWordsVo>> smStoreReportByCa(String storeId) {
        checkStoreAccess(storeId);
        List<OrderPO> allOrders = orderMapper.selectList(new LambdaQueryWrapper<OrderPO>()
                .eq(StringUtils.isNotBlank(storeId), OrderPO::getStoreId, storeId)
                .notIn(OrderPO::getStatus, List.of(
                        OrderStatusEnum.TO_FORWARD.getCode(),
                        OrderStatusEnum.TO_PAY.getCode(),
                        OrderStatusEnum.EXPIRED.getCode()
                ))
        );
        Map<String, List<OrderPO>> caOrdersMap = allOrders.stream()
                .collect(Collectors.groupingBy(OrderPO::getCaName));


        return salesReportGrouping(allOrders, caOrdersMap);
    }

    public Map<String, List<SalesPerformanceByKeyWordsVo>> salesReportGrouping(List<OrderPO> allOrders, Map<String, List<OrderPO>> orderMap) {

        Map<String, List<SalesPerformanceByKeyWordsVo>> result = new HashMap<>();
        result.put("salesAmount", new ArrayList<>());
        result.put("salesCount", new ArrayList<>());
        result.put("orderCount", new ArrayList<>());

        // 获取时间范围
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();


        if (allOrders.isEmpty()) {
            return result;
        }

        List<OrderItemPO> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemPO>()
                        .in(OrderItemPO::getOrderId, allOrders.stream().map(OrderPO::getId).toList())
        );

        orderMap.forEach((key, caOrders) -> {
            List<Long> caOrderIds = caOrders.stream().map(OrderPO::getId).toList();


            // 今日数据
            List<OrderPO> todayOrders = caOrders.stream()
                    .filter(order -> order.getCreatedTime().toLocalDateTime().isAfter(todayStart))
                    .toList();

            // 昨日数据
            List<OrderPO> yesterdayOrders = caOrders.stream()
                    .filter(order -> {
                        LocalDateTime orderTime = order.getCreatedTime().toLocalDateTime();
                        return orderTime.isAfter(yesterdayStart) && orderTime.isBefore(todayStart);
                    })
                    .toList();

            // 本周数据
            List<OrderPO> weekOrders = caOrders.stream()
                    .filter(order -> order.getCreatedTime().toLocalDateTime().isAfter(weekStart))
                    .toList();

            // 本月数据
            List<OrderPO> monthOrders = caOrders.stream()
                    .filter(order -> order.getCreatedTime().toLocalDateTime().isAfter(monthStart))
                    .toList();

            result.get("orderCount").add(new SalesPerformanceByKeyWordsVo(key
                    , BigDecimal.valueOf(todayOrders.size()) //
                    , BigDecimal.valueOf(yesterdayOrders.size())
                    , BigDecimal.valueOf(weekOrders.size())
                    , BigDecimal.valueOf(monthOrders.size())));

            result.get("salesCount").add(new SalesPerformanceByKeyWordsVo(key
                    , BigDecimal.valueOf(
                    orderItems.stream()
                            .filter(item -> caOrderIds.contains(item.getOrderId()) &&
                                    item.getCreatedTime().toLocalDateTime().isAfter(todayStart))
                            .mapToLong(OrderItemPO::getQty)
                            .sum())
                    , BigDecimal.valueOf(
                    orderItems.stream()
                            .filter(item -> {

                                LocalDateTime itemTime = item.getCreatedTime().toLocalDateTime();
                                return caOrderIds.contains(item.getOrderId()) && itemTime.isAfter(yesterdayStart) && itemTime.isBefore(todayStart);
                            })
                            .mapToLong(OrderItemPO::getQty)
                            .sum())
                    , BigDecimal.valueOf(
                    orderItems.stream()
                            .filter(item -> caOrderIds.contains(item.getOrderId()) &&
                                    item.getCreatedTime().toLocalDateTime().isAfter(weekStart))
                            .mapToLong(OrderItemPO::getQty)
                            .sum())
                    , BigDecimal.valueOf(
                    orderItems.stream()
                            .filter(item -> caOrderIds.contains(item.getOrderId()) &&
                                    item.getCreatedTime().toLocalDateTime().isAfter(monthStart))
                            .mapToLong(OrderItemPO::getQty)
                            .sum())));

            result.get("salesAmount").add(new SalesPerformanceByKeyWordsVo(key
                    , (todayOrders.stream()
                    .map(OrderPO::getAmount)
                    .reduce(new BigDecimal("0.0"), BigDecimal::add))
                    , yesterdayOrders.stream()
                    .map(OrderPO::getAmount)
                    .reduce(new BigDecimal("0.0"), BigDecimal::add)
                    , weekOrders.stream()
                    .map(OrderPO::getAmount)
                    .reduce(new BigDecimal("0.0"), BigDecimal::add)
                    , monthOrders.stream()
                    .map(OrderPO::getAmount)
                    .reduce(new BigDecimal("0.0"), BigDecimal::add)));

        });


        return result;
    }

    @Override
    public Map<String, SalesPerformanceVo> getRmStoreReport() {
        Map<String, SalesPerformanceVo> result = new HashMap<>();

        // 获取时间范围
        LocalDateTime now = LocalDateTime.now();
        // 本周：本周一0点到当前时间
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        // 本月：本月1号0点到当前时间
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        // 上月：上月1号0点到上月最后一天23:59:59
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = monthStart.minusSeconds(1);
        String regionId = SessionUtils.getWecomSession().getRegionId();
        // 构建基础查询条件
        LambdaQueryWrapper<OrderPO> baseWrapper = new LambdaQueryWrapper<OrderPO>()
                .eq(StringUtils.isNotBlank(regionId), OrderPO::getRegionId, regionId)
                .notIn(OrderPO::getStatus, List.of(
                        OrderStatusEnum.TO_FORWARD.getCode(),
                        OrderStatusEnum.TO_PAY.getCode(),
                        OrderStatusEnum.EXPIRED.getCode()
                ));

        // 本周数据
        LambdaQueryWrapper<OrderPO> weekWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(weekStart.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_THIS_WEEK, getReportDataVo(weekWrapper));

        // 本月数据
        LambdaQueryWrapper<OrderPO> monthWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(monthStart.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_THIS_MONTH, getReportDataVo(monthWrapper));

        // 上月数据
        LambdaQueryWrapper<OrderPO> lastMonthWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(lastMonthStart.atZone(ZoneId.systemDefault()).toInstant()))
                .le(OrderPO::getCreatedTime, Date.from(lastMonthEnd.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_LAST_MONTH, getReportDataVo(lastMonthWrapper));

        return result;
    }

    @Override
    public Map<String, List<SalesPerformanceByKeyWordsVo>> rmRegionReportByStore() {
        String regionId = SessionUtils.getWecomSession().getRegionId();
        if (Objects.isNull(regionId)) {
            return Map.of();
        }
        List<OrderPO> allOrders = orderMapper.selectList(new LambdaQueryWrapper<OrderPO>()
                .eq(StringUtils.isNotBlank(regionId), OrderPO::getRegionId, regionId)
                .notIn(OrderPO::getStatus, List.of(
                        OrderStatusEnum.TO_FORWARD.getCode(),
                        OrderStatusEnum.TO_PAY.getCode(),
                        OrderStatusEnum.EXPIRED.getCode()
                ))
        );
        Map<String, List<OrderPO>> caOrdersMap = allOrders.stream()
                .collect(Collectors.groupingBy(OrderPO::getStoreName));


        return salesReportGrouping(allOrders, caOrdersMap);
    }

    @Override
    public PageResult<OrderCustomerVo> orderCustomer(String keyword, String storeId, Integer pageNum, Integer pageSize) {
        Page<OrderCustomerVo> pageParams = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrderPO> queryWrapper = new LambdaQueryWrapper<OrderPO>()
                .like(StringUtils.isNotBlank(keyword), OrderPO::getCustomerNickname, keyword)
                .notIn(OrderPO::getStatus, List.of(
                        OrderStatusEnum.TO_FORWARD.getCode(),
                        OrderStatusEnum.TO_PAY.getCode(),
                        OrderStatusEnum.EXPIRED.getCode()
                ));

        if (StringUtils.isNotBlank(storeId)) {
            queryWrapper.eq(StringUtils.isNotBlank(storeId), OrderPO::getStoreId, storeId);
        } else {
            WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
            if (Objects.nonNull(wecomSession) && StringUtils.isNotBlank(wecomSession.getUserId())) {
                queryWrapper.eq(OrderPO::getCaWwid, wecomSession.getUserId()).eq(StringUtils.isNotBlank(wecomSession.getCurrentStoreId()), OrderPO::getStoreId, wecomSession.getCurrentStoreId());
            } else {
                throw new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
            }

        }
        IPage<OrderCustomerVo> result = orderMapper.selectOrderCustomer(pageParams, queryWrapper);
        boolean hasMore = result.getCurrent() < result.getPages();
        return PageResult.<OrderCustomerVo>builder()
                .totalCount(result.getTotal())
                .totalPages(result.getPages())
                .pageNumber((int) result.getCurrent())
                .pageSize((int) result.getSize())
                .hasMore(hasMore)
                .data(result.getRecords())
                .build();
    }

    @Override
    public Map<String, SalesPerformanceVo> caPerformance() {
        WecomSessionInfoDTO sessionInfoDTO = SessionUtils.getWecomSession();
        if (Objects.isNull(sessionInfoDTO)) {
            throw new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
        }
        String userId = sessionInfoDTO.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
        }
        Map<String, SalesPerformanceVo> result = new HashMap<>();

        // 获取时间范围
        LocalDateTime now = LocalDateTime.now();
        // 本周：本周一0点到当前时间
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        // 本月：本月1号0点到当前时间
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        // 上月：上月1号0点到上月最后一天23:59:59
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = monthStart.minusSeconds(1);
        // 本年：本年1月1号0点到当前时间
        LocalDateTime yearStart = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay();

        // 构建基础查询条件
        LambdaQueryWrapper<OrderPO> baseWrapper = new LambdaQueryWrapper<OrderPO>()
                .eq(StringUtils.isNotBlank(userId), OrderPO::getCaWwid, userId)
                .eq(StringUtils.isNotBlank(sessionInfoDTO.getCurrentStoreId()), OrderPO::getStoreId, sessionInfoDTO.getCurrentStoreId())
                .notIn(OrderPO::getStatus, List.of(
                        OrderStatusEnum.TO_FORWARD.getCode(),
                        OrderStatusEnum.TO_PAY.getCode(),
                        OrderStatusEnum.EXPIRED.getCode()
                ));

        // 本周数据
        LambdaQueryWrapper<OrderPO> weekWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(weekStart.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_THIS_WEEK, getReportDataVo(weekWrapper));

        // 本月数据
        LambdaQueryWrapper<OrderPO> monthWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(monthStart.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_THIS_MONTH, getReportDataVo(monthWrapper));

        // 上月数据
        LambdaQueryWrapper<OrderPO> lastMonthWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(lastMonthStart.atZone(ZoneId.systemDefault()).toInstant()))
                .le(OrderPO::getCreatedTime, Date.from(lastMonthEnd.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_LAST_MONTH, getReportDataVo(lastMonthWrapper));

        // 本年数据
        LambdaQueryWrapper<OrderPO> yearWrapper = baseWrapper.clone()
                .ge(OrderPO::getCreatedTime, Date.from(yearStart.atZone(ZoneId.systemDefault()).toInstant()));
        result.put(TIME_RANGE_THIS_YEAR, getReportDataVo(yearWrapper));

        return result;
    }

    @Override
    public StorePerformanceVo smStorePerformance(String storeId) {
        checkStoreAccess(storeId);
        return orderMapper.selectOrderPerformance(storeId, null);
    }

    @Override
    public StorePerformanceVo rmStorePerformance(String storeId) {
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        return orderMapper.selectOrderPerformance(storeId, wecomSession.getRegionId());
    }

    @Override
    public OrderCustomerVo customerDetail(String customerExternalUserid) {
        LambdaQueryWrapper<OrderPO> queryWrapper = new LambdaQueryWrapper<OrderPO>()
                .notIn(OrderPO::getStatus, List.of(
                        OrderStatusEnum.TO_FORWARD.getCode(),
                        OrderStatusEnum.TO_PAY.getCode(),
                        OrderStatusEnum.EXPIRED.getCode()
                )).eq(OrderPO::getCustomerExternalUserid, customerExternalUserid);
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();

        if (RoleTypeConstants.SALES.equals(wecomSession.getCaPosition())) {
            queryWrapper.eq(OrderPO::getCaWwid, wecomSession.getUserId()).eq(StringUtils.isNotBlank(wecomSession.getCurrentStoreId()), OrderPO::getStoreId, wecomSession.getCurrentStoreId());
        } else if (RoleTypeConstants.STORE_MANAGER.equals(wecomSession.getCaPosition())) {
            queryWrapper.eq(StringUtils.isNotBlank(wecomSession.getCurrentStoreId()), OrderPO::getStoreId, wecomSession.getCurrentStoreId());
        }
        List<OrderCustomerVo> result = orderMapper.selectOrderCustomer(queryWrapper);
        return result.isEmpty() ? null : result.getFirst();
    }

    private SalesPerformanceVo getReportDataVo(LambdaQueryWrapper<OrderPO> wrapper) {
        // 1. 查询订单列表
        List<OrderPO> orders = orderMapper.selectList(wrapper);
        if (orders.isEmpty()) {
            return new SalesPerformanceVo();
        }
        List<OrderItemPO> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemPO>()
                        .in(OrderItemPO::getOrderId, orders.stream().map(OrderPO::getId).toList())
        );
        SalesPerformanceVo reportVO = new SalesPerformanceVo();
        //统计 订单量
        reportVO.setOrderCount(BigDecimal.valueOf(orders.size()));

        //统计销售额
        BigDecimal totalAmount = orders.stream()
                .map(OrderPO::getAmount)
                .reduce(new BigDecimal("0.0"), BigDecimal::add);
        reportVO.setSalesAmount(totalAmount);
        //统计销售量
        reportVO.setSalesCount(BigDecimal.valueOf(orderItems.stream().map(OrderItemPO::getQty).collect(Collectors.summarizingLong(qty -> qty)).getSum()));
        return reportVO;
    }
}
