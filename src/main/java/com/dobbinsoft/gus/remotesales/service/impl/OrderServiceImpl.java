package com.dobbinsoft.gus.remotesales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.common.utils.context.GenericRequestContextHolder;
import com.dobbinsoft.gus.common.utils.context.bo.TenantContext;
import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.client.gus.location.LocationFeignClient;
import com.dobbinsoft.gus.remotesales.client.gus.location.model.LocationVO;
import com.dobbinsoft.gus.remotesales.client.gus.logistics.ExpressFeignClient;
import com.dobbinsoft.gus.remotesales.client.gus.logistics.model.ExpressOrderCreateDTO;
import com.dobbinsoft.gus.remotesales.client.gus.logistics.model.ExpressOrderVO;
import com.dobbinsoft.gus.remotesales.client.gus.payment.TransactionFeignClient;
import com.dobbinsoft.gus.remotesales.client.gus.payment.model.TransactionCreateDTO;
import com.dobbinsoft.gus.remotesales.client.gus.product.model.CurrencyCode;
import com.dobbinsoft.gus.remotesales.client.product.ProductClient;
import com.dobbinsoft.gus.remotesales.client.wecom.WeComAdapterClient;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.WeComExternalContactResponse;
import com.dobbinsoft.gus.remotesales.data.constant.RSConstants;
import com.dobbinsoft.gus.remotesales.data.constant.RoleTypeConstants;
import com.dobbinsoft.gus.remotesales.data.dto.order.*;
import com.dobbinsoft.gus.remotesales.data.dto.session.WechatSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.enums.*;
import com.dobbinsoft.gus.remotesales.data.po.*;
import com.dobbinsoft.gus.remotesales.data.vo.SystemRefundOrderVO;
import com.dobbinsoft.gus.remotesales.data.vo.order.*;
import com.dobbinsoft.gus.remotesales.data.vo.product.ProductVo;
import com.dobbinsoft.gus.remotesales.data.vo.report.OrderItemExportVO;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.mapper.*;
import com.dobbinsoft.gus.remotesales.service.ConfigCenterService;
import com.dobbinsoft.gus.remotesales.service.OrderService;
import com.dobbinsoft.gus.remotesales.service.biz.NotificationBizService;
import com.dobbinsoft.gus.remotesales.service.biz.OrderBizService;
import com.dobbinsoft.gus.remotesales.service.biz.SchedulerBizService;
import com.dobbinsoft.gus.remotesales.utils.DateUtils;
import com.dobbinsoft.gus.remotesales.utils.OrderUtils;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import com.dobbinsoft.gus.remotesales.utils.delay.EasyScheduler;
import com.dobbinsoft.gus.web.exception.BasicErrorCode;
import com.dobbinsoft.gus.web.exception.ServiceException;
import com.dobbinsoft.gus.web.vo.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // Mappers
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderLogMapper orderLogMapper;
    @Autowired
    private OrderRefundMapper orderRefundMapper;
    @Autowired
    private OrderAdjustPriceMapper orderAdjustPriceMapper;
    @Autowired
    private OrderPayLogMapper orderPayLogMapper;
    // Clients
    @Autowired
    private WeComAdapterClient weComAdapterClient;
    @Autowired
    private LocationFeignClient locationFeignClient;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private ExpressFeignClient expressFeignClient;
    @Autowired
    private ConfigCenterClient configCenterClient;
    // BizServices
    @Autowired
    private OrderBizService orderBizService;
    @Autowired
    private NotificationBizService notificationBizService;
    @Autowired
    private ConfigCenterService configCenterService;
    // Spring Component
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TransactionFeignClient transactionFeignClient;

    private static final String ADJUST_TEXT = "发起了调价申请";

    @Override
    @Transactional
    public OrderVO submitOrder(OrderSubmitDTO submitDTO) {
        // 1. 参数校验
        submitDTO.valid();
        // 获取当前登录CA
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        if (StringUtils.isEmpty(wecomSession.getStoreId())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_STORE_NOT_EXIST);
        }
        // 2. 创建订单
        OrderPO order = new OrderPO();
        // 2.1. 订单基本信息
        order.setOrderNo(OrderUtils.generateOrderNo());
        order.setType(submitDTO.getType());
        order.setStatus(OrderStatusEnum.TO_FORWARD.getCode());
        order.setPayStatus(PayStatusEnum.UNPAID.getCode());
        order.setInnerRemark(submitDTO.getInnerRemark());
        order.setRemark(submitDTO.getRemark());
        order.setResetDeliverMethodTimes(0);
        order.setTotalRefundCount(0);
        order.setTotalRefund(BigDecimal.ZERO);
        // 2.2. 订单CA关联信息
        order.setCaWwid(wecomSession.getUserId());
        order.setCaName(wecomSession.getName());
        order.setCaPosition(wecomSession.getCaPosition());
        order.setCaAvatar(wecomSession.getCaAvatar());
        order.setRegionId(wecomSession.getRegionId());
        order.setStoreId(wecomSession.getStoreId());
        order.setStoreName(wecomSession.getStoreName());


        // 2.3. 订单客人信息
        order.setCustomerExternalUserid(submitDTO.getExternalUserId());
        ConfigContentVO brandAllConfigContent = configCenterClient.getBrandAllConfigContent();
        WeComExternalContactResponse externalContact = weComAdapterClient.getExternalContact(brandAllConfigContent.getBrand().getAgentId(), submitDTO.getExternalUserId());
        if (externalContact == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_EXTERNAL_CONTACT_NOT_EXIST);
        }
        WeComExternalContactResponse.ExternalContact contact = externalContact.getExternalContact();
        order.setCustomerUnionid(contact.getUnionid());
        order.setCustomerName(contact.getName());
        order.setCustomerAvatar(contact.getAvatar());
        order.setCustomerGender(contact.getGender());
        // 昵称优先用name，没有单独nickname字段
        order.setCustomerNickname(contact.getName());
        // 手机号优先用remarkMobiles第一个
        if (contact.getSubscriberInfo() != null && contact.getSubscriberInfo().getRemarkMobiles() != null
                && !contact.getSubscriberInfo().getRemarkMobiles().isEmpty()) {
            order.setCustomerMobile(contact.getSubscriberInfo().getRemarkMobiles().getFirst());
        }
        // 2.4. 计算金额
        BigDecimal skuTotalAmount = BigDecimal.ZERO;

        List<OrderItemPO> orderItems = new ArrayList<>();
        for (OrderItemSubmitDTO itemDTO : submitDTO.getItems()) {
            OrderItemPO item = assembleSubmitOrderItem(itemDTO, wecomSession);
            skuTotalAmount = skuTotalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
            orderItems.add(item);
        }
        if (order.getType() == OrderTypeEnum.ORDER.getCode().intValue()) {
            order.setAmount(skuTotalAmount);
            order.setPayAmount(BigDecimal.ZERO);
        } else {
            // 订金单
            order.setAmount(submitDTO.getDepositAmount());
            order.setPayAmount(BigDecimal.ZERO);
        }
        // 3. 持久化订单
        order.setSubmitTime(ZonedDateTime.now());
        orderMapper.insert(order);
        // 持久化订单明细
        for (OrderItemPO item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        boolean adjustCheck=false;
        // 4. 调价
        for (int i = 0; i < submitDTO.getItems().size(); i++) {
            OrderItemSubmitDTO itemDTO = submitDTO.getItems().get(i);
            OrderItemPO orderItemPO = orderItems.get(i);
            // 调价价格
            if (itemDTO.getAdjustPrice() != null) {
                OrderAdjustPricePO orderAdjustPricePO = new OrderAdjustPricePO();
                orderAdjustPricePO.setOrderId(order.getId());
                orderAdjustPricePO.setPrice(itemDTO.getAdjustPrice());
                orderAdjustPricePO.setOriginalPrice(orderItemPO.getOriginalPrice());
                orderAdjustPricePO.setOrderItemId(orderItemPO.getId());
                orderAdjustPricePO.setStoreId(order.getStoreId());
                orderAdjustPricePO.setQty(orderItemPO.getQty());
                orderAdjustPricePO.setStatus(OrderAdjustPriceStatusEnum.PENDING.getCode());
                orderAdjustPricePO.setInnerRemark(itemDTO.getAdjustPriceInnerRemark());
                orderAdjustPriceMapper.insert(orderAdjustPricePO);
                // 调价审批通知
                notificationBizService.sendPriceAdjustmentAudit(wecomSession.getName() + ADJUST_TEXT, order, orderItemPO, orderAdjustPricePO, wecomSession.getCurrentStore().getManagerWwid());
                adjustCheck=true;
            }
        }
        saveOrderLog(order,OrderStatusEnum.TO_FORWARD,OrderLogTypeEnum.CREATE_ORDER);
        if(adjustCheck){
            saveOrderLog(order,OrderStatusEnum.TO_FORWARD,OrderLogTypeEnum.APPLY_ADJUST);
        }
        //获取配置
        ConfigContentVO.OrderExpiry orderExpiry = configCenterService.getBrandAllConfigContent().getOrderExpiry();
        //如果为启用状态则 开启任务
        if (Objects.nonNull(orderExpiry) && Boolean.TRUE.equals(orderExpiry.getEnabled())) {
            ZonedDateTime zonedDateTime = ZonedDateTime.now().plusSeconds(orderExpiry.getExpiryTime());
            EasyScheduler.createJob(SchedulerBizService.ORDER_AUTO_CANCEL, zonedDateTime, new Object[]{order.getId(), getTenantId()});
        }

        return OrderVO.of(order, orderItems);
    }
    public void saveOrderLog(OrderPO order,OrderStatusEnum orderStatusEnum,OrderLogTypeEnum orderLogTypeEnum){
        // 5. 订单日志
        OrderLogPO orderLog = OrderLogPO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(orderStatusEnum.getCode())
                .statusDesc(orderStatusEnum.getMsg())
                .type(orderLogTypeEnum.getCode())
                .typeDesc(orderLogTypeEnum.getMsg())
                .build();
        orderLogMapper.insert(orderLog);

    }
    public String getTenantId() {
        return GenericRequestContextHolder.getTenantContext()
                .map(TenantContext::getTenantId)
                .orElseThrow(() -> new ServiceException(RemotesalesErrorCode.SYSTEM_ERROR));
    }

    @Override
    public PageResult<WechatOrderListVO> getWechatOrderList(String keyword, Integer pageNum, Integer pageSize) {
        WechatSessionInfoDTO wechatSession = SessionUtils.getWechatSession();
        // 1. 构建分页对象
        Page<OrderPO> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderPO::getCustomerUnionid, wechatSession.getUnionid())
                .ne(OrderPO::getStatus, OrderStatusEnum.TO_FORWARD.getCode());
        wrapper.and(StringUtils.isNotBlank(keyword), w -> w
                .like(OrderPO::getOrderNo, keyword)
                .or().like(OrderPO::getCaWwid, keyword)
                .or().like(OrderPO::getCaName, keyword)
                .or().like(OrderPO::getCustomerName, keyword)
                .or().like(OrderPO::getCustomerNickname, keyword)
        );
        wrapper.orderByDesc(OrderPO::getId);

        // 3. 查询订单分页
        Page<OrderPO> orderPage = orderMapper.selectPage(page, wrapper);
        List<OrderPO> orders = orderPage.getRecords();
        if (orders.isEmpty()) {
            return PageResult.<WechatOrderListVO>builder()
                    .totalCount(0)
                    .totalPages(0)
                    .pageNumber(pageNum)
                    .pageSize(pageSize)
                    .hasMore(false)
                    .data(new ArrayList<>())
                    .build();
        }


        // 4. 组装 VO
        List<WechatOrderListVO> voList = orders.stream().map(WechatOrderListVO::of).toList();
        this.assembleOrderItems(voList);

        // 5. 返回分页结果
        boolean hasMore = orderPage.getCurrent() < orderPage.getPages();
        return PageResult.<WechatOrderListVO>builder()
                .totalCount(orderPage.getTotal())
                .totalPages(orderPage.getPages())
                .pageNumber((int) orderPage.getCurrent())
                .pageSize((int) orderPage.getSize())
                .hasMore(hasMore)
                .data(voList)
                .build();
    }

    @Override
    @Transactional
    public OrderDetailVO getWechatOrderDetail(String orderNo) {
        WechatSessionInfoDTO wechatSession = SessionUtils.getWechatSession();
        OrderPO orderPO = orderMapper.selectOne(new LambdaQueryWrapper<OrderPO>().eq(OrderPO::getOrderNo, orderNo));
        if (orderPO == null || orderPO.getStatus() == OrderStatusEnum.TO_FORWARD.getCode().intValue()) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }
        // 检验订单查看权限，若关闭此校验 A顾客下单，转发给B顾客 付款
        if (!StringUtils.equals(wechatSession.getUnionid(), orderPO.getCustomerUnionid())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_FOR_YOU);
        }
        orderPO.setCustomerBrowseOpenid(wechatSession.getOpenid());
        orderPO.setCustomerBrowseUnionid(wechatSession.getUnionid());
        orderMapper.updateById(orderPO);
        tryQueryPayResult(orderPO);
        return assembleOrderDetail(orderPO);
    }

    @Override
    public PageResult<OrderVO> getWecomOrderList(String caId, String storeId, String regionId, WecomOrderListDTO dto) {
        Page<OrderPO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        //角色控制查询条件
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<OrderPO>()
                .eq(StringUtils.isNotBlank(caId), OrderPO::getCaWwid, caId)
                .eq(StringUtils.isNotBlank(storeId), OrderPO::getStoreId, storeId)
                .eq(StringUtils.isNotBlank(regionId), OrderPO::getRegionId, regionId);

        wrapper.and(dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty(), w -> w
                .like(OrderPO::getOrderNo, dto.getKeyword())
                .or().like(OrderPO::getCaWwid, dto.getKeyword())
                .or().like(OrderPO::getCaName, dto.getKeyword())
                .or().like(OrderPO::getCustomerName, dto.getKeyword())
                .or().like(OrderPO::getCustomerNickname, dto.getKeyword())
        );
        wrapper.ge(dto.getStartTime() != null, OrderPO::getCreatedTime, dto.getStartTime());
        wrapper.le(dto.getEndTime() != null, OrderPO::getCreatedTime, dto.getEndTime());
        wrapper.in(CollectionUtils.isNotEmpty(dto.getOrderTypeList()), OrderPO::getType, dto.getOrderTypeList());
        wrapper.in(CollectionUtils.isNotEmpty(dto.getPayStatusList()), OrderPO::getPayStatus, dto.getPayStatusList());
        wrapper.in(CollectionUtils.isNotEmpty(dto.getStatusList()), OrderPO::getStatus, dto.getStatusList());
        wrapper.in(CollectionUtils.isNotEmpty(dto.getDeliveryMethodList()), OrderPO::getDeliveryMethod, dto.getDeliveryMethodList());
        wrapper.in(CollectionUtils.isNotEmpty(dto.getCustomerExternalUseridList()), OrderPO::getCustomerExternalUserid, dto.getCustomerExternalUseridList());
        wrapper.in(CollectionUtils.isNotEmpty(dto.getCaWwidList()), OrderPO::getCaWwid, dto.getCaWwidList());
        if (CollectionUtils.isNotEmpty(dto.getRefundStatusList()) && dto.getRefundStatusList().size() == 1) {
            if (dto.getRefundStatusList().getFirst() == 0) {
                wrapper.eq(OrderPO::getTotalRefundCount, 0);
            } else {
                wrapper.gt(OrderPO::getTotalRefundCount, 0);
            }
        }

        // 使用id代替创建时间排序，可加快查询速度
        wrapper.orderBy(dto.getSort() == null || dto.getSort() == WecomOrderListDTO.Sort.CREATE_TIME, Boolean.TRUE.equals(dto.getAsc()), OrderPO::getId);
        wrapper.orderBy(dto.getSort() == WecomOrderListDTO.Sort.PAY_TIME, Boolean.TRUE.equals(dto.getAsc()), OrderPO::getPayTime);
        wrapper.orderBy(dto.getSort() == WecomOrderListDTO.Sort.AMOUNT, Boolean.TRUE.equals(dto.getAsc()), OrderPO::getAmount);
        Page<OrderPO> orderPage = orderMapper.selectPage(page, wrapper);
        List<OrderPO> orders = orderPage.getRecords();
        if (orders.isEmpty()) {
            return PageResult.<OrderVO>builder()
                    .totalCount(0)
                    .totalPages(0)
                    .pageNumber(dto.getPageNum())
                    .pageSize(dto.getPageSize())
                    .hasMore(false)
                    .data(new ArrayList<>())
                    .build();
        }
        List<OrderVO> voList = orders.stream().map(OrderVO::of).toList();
        this.assembleOrderItems(voList);
        boolean hasMore = orderPage.getCurrent() < orderPage.getPages();
        return PageResult.<OrderVO>builder()
                .totalCount(orderPage.getTotal())
                .totalPages(orderPage.getPages())
                .pageNumber((int) orderPage.getCurrent())
                .pageSize((int) orderPage.getSize())
                .hasMore(hasMore)
                .data(voList)
                .build();
    }

    @Override
    @Transactional
    public OrderDetailVO getWecomOrderDetail(String caId, String storeId, String regionId, String orderNo) {
        //角色控制查询条件
        ConfigContentVO.Refund refund = configCenterClient.getBrandAllConfigContent().getRefund();
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<OrderPO>()
                .eq(StringUtils.isNotBlank(caId), OrderPO::getCaWwid, caId)
                .eq(StringUtils.isNotBlank(storeId), OrderPO::getStoreId, storeId)
                .eq(StringUtils.isNotBlank(regionId), OrderPO::getRegionId, regionId);
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        // 财务可以看所有
        OrderPO orderPO;
        if (wecomSession.getUserId().equals(refund.getRefundApproverWwid())) {
            orderPO = orderMapper.selectOne(new LambdaQueryWrapper<OrderPO>().eq(OrderPO::getOrderNo, orderNo));
        } else {
            // 查询订单
            orderPO = orderMapper.selectOne(wrapper.eq(OrderPO::getOrderNo, orderNo));
        }
        if (orderPO == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }
        tryQueryPayResult(orderPO);
        return assembleOrderDetail(orderPO);
    }

    @Override
    @Transactional
    public void shareToCustomer(Long orderId) {
        if (orderId == null) {
            throw new ServiceException(RemotesalesErrorCode.PARAMERROR);
        }
        // 获取当前登录CA
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();

        // 查询订单
        OrderPO order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }
        // 校验当前登录人是否为订单销售
        if (!StringUtils.equals(wecomSession.getUserId(), order.getCaWwid())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_SHARE_ONLY);
        }
        // 校验订单状态
        if (!OrderStatusEnum.TO_FORWARD.getCode().equals(order.getStatus())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_SHARE_CHECK);
        }
        // 更新订单状态
        order.setStatus(OrderStatusEnum.TO_PAY.getCode());
        orderMapper.updateById(order);

        // 记录订单日志
        OrderLogPO orderLog = OrderLogPO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .statusDesc(OrderStatusEnum.TO_PAY.getMsg())
                .type(OrderLogTypeEnum.SHARE_ORDER.getCode())
                .typeDesc(OrderLogTypeEnum.SHARE_ORDER.getMsg())
                .createdByName(wecomSession.getName())
                .build();
        orderLogMapper.insert(orderLog);


    }

    @Override
    @Transactional
    public void chooseDeliveryMethod(OrderChooseDeliveryDTO param) {
        // 参数校验抽取到DTO
        param.valid();
        // 获取当前用户 unionId
        WechatSessionInfoDTO wechatSession = SessionUtils.getWechatSession();
        // 查询订单
        OrderPO order = orderBizService.getOrderByCustomer(param.getOrderId(), wechatSession);
        // 设置配送方式和相关信息
        if (DeliveryMethodEnum.LOGISTICS.getCode().equals(param.getDeliveryMethod())) {
            order.setAddress(JsonUtil.convertToString(param.getAddress()));
            if (order.getStatus().equals(OrderStatusEnum.RESETTING_DELIVER_METHOD.getCode())) {
                order.setStatus(OrderStatusEnum.TO_DELIVER.getCode());
            }
        } else if (DeliveryMethodEnum.SELF_PICKUP.getCode().equals(param.getDeliveryMethod())) {
            order.setPickupStoreId(param.getStoreId());
            if (order.getStatus().equals(OrderStatusEnum.RESETTING_DELIVER_METHOD.getCode())) {
                order.setStatus(OrderStatusEnum.TO_PICKUP.getCode());
            }
            // 校验门店是否存在（Store HUB）
            R<LocationVO> responseVo = locationFeignClient.detail(param.getStoreId());
            LocationVO data = responseVo.getData();
            if (data != null) {
                order.setPickupStoreName(data.getName());
            } else {
                throw new ServiceException(RemotesalesErrorCode.ORDER_PICKUP_ADDRESS_NOT_EXIST);
            }
        }
        order.setDeliveryMethod(param.getDeliveryMethod());
        order.setDeliveryMethodChooseTime(ZonedDateTime.now());
        // 更新订单
        orderMapper.updateById(order);
        // 记录订单日志
        OrderLogPO orderLog = OrderLogPO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .statusDesc(BaseEnums.getMsgByCode(order.getStatus(), OrderStatusEnum.class))
                .type(OrderLogTypeEnum.MODIFY_DELIVERY_METHOD.getCode())
                .typeDesc(OrderLogTypeEnum.MODIFY_DELIVERY_METHOD.getMsg())
                .build();
        orderLogMapper.insert(orderLog);
        if (order.getResetDeliverMethodTimes() > 0) {
            notificationBizService.sendDeliveryMsg(order, "客人修改了提货方式，%s"
                    .formatted(DeliveryMethodEnum.SELF_PICKUP.getCode().equals(order.getDeliveryMethod()) ? "等待自提" : "等待发货"));
        }


    }

    @Override
    @Transactional
    public void resetDeliveryMethod(OrderResetDeliveryDTO orderResetDeliveryDTO) {
        // 参数校验
        if (orderResetDeliveryDTO == null || orderResetDeliveryDTO.getOrderId() == null) {
            throw new ServiceException(RemotesalesErrorCode.PARAMERROR);
        }
        // 获取当前用户 unionId
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        OrderPO order = orderBizService.getOrderByCA(orderResetDeliveryDTO.getOrderId(), wecomSession);
        OrderStatusEnum orderStatusEnum = BaseEnums.getByCode(order.getStatus(), OrderStatusEnum.class);
        assert orderStatusEnum != null;
        if (!orderStatusEnum.resetDeliverAble()) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_CANNOT_RESET_DELIVER);
        }
        // 重置配送方式和相关信息
        orderMapper.update(
                null,
                new LambdaUpdateWrapper<OrderPO>()
                        .eq(OrderPO::getId, order.getId())
                        .set(OrderPO::getStatus, OrderStatusEnum.RESETTING_DELIVER_METHOD.getCode())
                        .set(OrderPO::getResetDeliverMethodTimes, order.getResetDeliverMethodTimes() + 1)
                        .set(OrderPO::getDeliveryMethod, null)
                        .set(OrderPO::getAddress, null)
                        .set(OrderPO::getPickupStoreId, null)
                        .set(OrderPO::getPickupStoreName, null)
        );
        // 记录订单日志
        OrderLogPO orderLog = OrderLogPO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .statusDesc(BaseEnums.getMsgByCode(order.getStatus(), OrderStatusEnum.class))
                .type(OrderLogTypeEnum.RESET_DELIVERY_METHOD.getCode())
                .typeDesc(OrderLogTypeEnum.RESET_DELIVERY_METHOD.getMsg())
                .createdByName(wecomSession.getName())
                .build();
        orderLogMapper.insert(orderLog);
    }

    @Override
    @Transactional
    public void uploadPosNo(OrderPosNoDTO orderPosNoDTO) {
        // 参数校验由DTO注解完成
        OrderPO order = orderMapper.selectById(orderPosNoDTO.getOrderId());
        boolean insertOrUpdate = Objects.isNull(order.getPosNumber()) && Objects.isNull(order.getReceipt()) && Objects.isNull(order.getReceiptNumber());
        // 更新POS小票号和小票图片
        order.setPosNumber(orderPosNoDTO.getPosNumber());
        order.setReceipt(orderPosNoDTO.getReceipt());
        order.setReceiptNumber(orderPosNoDTO.getReceiptNumber());
        //如果是订金单，上传pos单号 变已完成
        if (OrderTypeEnum.DEPOSIT_ORDER.getCode().equals(order.getType()) && OrderStatusEnum.TO_BE_COMPLETED.getCode().equals(order.getStatus())) {
            if (StringUtils.isNotEmpty(orderPosNoDTO.getPosNumber())) {
                order.setStatus(OrderStatusEnum.COMPLETED.getCode());
            } else {
                log.error("order type is DEPOSIT_ORDER ,posNumber is null orderId:{},request:{}", order.getId(), orderPosNoDTO);
                throw new ServiceException(RemotesalesErrorCode.ORDER_POS_NO_NOT_EXIST);
            }
        }
        orderMapper.updateById(order);
        // 记录订单日志
        saveOrderLog(order, Objects.requireNonNull(BaseEnums.getByCode(order.getStatus(), OrderStatusEnum.class)), insertOrUpdate?OrderLogTypeEnum.UPLOAD_POST_NO:OrderLogTypeEnum.UPLOAD_POST_NO_UPDATE);
    }

    @Override
    @Transactional
    public void expressOrder(OrderExpressDTO orderExpressDTO) {
        // 当前登录CA
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        // 1. 校验订单 已支付 & 已填POS NO
        OrderPO order = orderBizService.getOrderByCA(orderExpressDTO.getOrderId(), wecomSession);
        if (!PayStatusEnum.PAID.getCode().equals(order.getPayStatus())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_PAID);
        }
        if (StringUtils.isBlank(order.getPosNumber())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_POS_NO_NOT_EXIST);
        }
        if (!DeliveryMethodEnum.LOGISTICS.getCode().equals(order.getDeliveryMethod())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_DELIVER_METHOD_NOT_LOGISTICS);
        }

        R<LocationVO> storeResponse = locationFeignClient.detail(order.getStoreId());
        if (!BasicErrorCode.SUCCESS.getCode().equals(storeResponse.getCode())) {
            throw new ServiceException(storeResponse.getCode(), storeResponse.getMessage());
        }
        LocationVO store = storeResponse.getData();
        // 2. 准备快递请求参数
        ExpressOrderCreateDTO createDTO = new ExpressOrderCreateDTO();
        createDTO.setOrderNo(order.getOrderNo());
        createDTO.setUniqueKey(UUID.randomUUID().toString().replace("-", ""));
        createDTO.setRemark(order.getRemark());

        // 设置发件人信息
        ExpressOrderCreateDTO.Sender sender = new ExpressOrderCreateDTO.Sender();
        sender.setName(order.getCaName());
        sender.setMobile(store.getMobile());
        sender.setProvince(store.getProvince());
        sender.setCity(store.getCity());
        sender.setDistrict(store.getDistrict());
        sender.setAddress(store.getAddress());
        createDTO.setSender(sender);

        // 设置收件人信息
        ExpressOrderCreateDTO.Receiver receiver = new ExpressOrderCreateDTO.Receiver();

        if (StringUtils.isEmpty(order.getAddress())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_EXPRESS_ADDRESS_IS_NULL);
        }
        OrderAddressVO orderAddressVO = JsonUtil.convertToObject(order.getAddress(), OrderAddressVO.class);
        receiver.setAddress(orderAddressVO.toAddressString());
        receiver.setProvince(orderAddressVO.getProvinceName());
        receiver.setCity(orderAddressVO.getCityName());
        receiver.setDistrict(orderAddressVO.getCountyName());
        receiver.setName(orderAddressVO.getUserName());
        receiver.setMobile(orderAddressVO.getTelNumber());
        createDTO.setReceiver(receiver);

        // 获取订单商品信息
        List<OrderItemPO> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemPO>()
                        .eq(OrderItemPO::getOrderId, order.getId())
        );

        // 3. 更新订单状态
        order.setStatus(OrderStatusEnum.TO_RECEIVE.getCode());
        order.setDeliveryTime(ZonedDateTime.now());
        orderMapper.updateById(order);

        // 4. 记录订单日志
        OrderLogPO orderLog = OrderLogPO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .statusDesc(OrderStatusEnum.TO_RECEIVE.getMsg())
                .type(OrderLogTypeEnum.SALES_DELIVERY.getCode())
                .typeDesc(OrderLogTypeEnum.SALES_DELIVERY.getMsg())
                .createdByName(wecomSession.getName())
                .build();
        orderLogMapper.insert(orderLog);

        // 5. 调用快递服务创建快递订单
        R<ExpressOrderVO> expressOrderVOR = expressFeignClient.create(createDTO);
        
        if (!BasicErrorCode.SUCCESS.getCode().equals(expressOrderVOR.getCode())) {
            throw new ServiceException(expressOrderVOR.getCode(), expressOrderVOR.getMessage());
        }

        ExpressOrderVO expressOrderVO = expressOrderVOR.getData();
        if (expressOrderVO == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_EXPRESS_NO_NOT_EXIST);
        }

        // 之后不允许再产生异常，因为后面异常无法回滚 express 系统的变更
        String logisticsNo = Strings.EMPTY;
        String logisticsCompany = Strings.EMPTY;
        try {
            // 6. 更新快递信息
            logisticsNo = expressOrderVO.getTransNo();
            logisticsCompany = expressOrderVO.getLpName();
            order.setLogisticsNo(logisticsNo);
            order.setLogisticsCompany(logisticsCompany);
            order.setLogisticsCompanyCode(expressOrderVO.getLpCode());
            orderMapper.updateById(order);
            log.info("[订单物流发货] 已更新物流单号 orderId:{}, logisticsNo:{}, logisticsCompany:{}", order.getId(), logisticsNo, logisticsCompany);
            // 7. 发送门店邮件
            sendStoreEmail(expressOrderVO, order, orderItems);

//            // 8. 发送通知 （figma上没有找到有POS录入的通知，但是老系统有）
//            // 8.1 通知销售
//            notificationBizService.sendRemindmsg(
//                    order.getOrderNo(),
//                    order.getCaName(),
//                    1,
//                    order.getStoreName(),
//                    order.getCaWwid(),
//                    false
//            );
//
//
//            // 8.2 通知店长
//            notificationBizService.sendRemindmsg(
//                    order.getOrderNo(),
//                    order.getCaName(),
//                    1,
//                    order.getStoreName(),
//                    wecomSession.getCurrentStore().getManagerWwid(),
//                    true
//            );

        } catch (Exception e) {
            log.error("[订单物流发货] 更新物流单号 异常  orderId:{}, logisticsNo:{}, logisticsCompany:{}", order.getId(), logisticsNo, logisticsCompany, e);
        }


    }


    private void sendStoreEmail(ExpressOrderVO expressOrderVO, OrderPO order, List<OrderItemPO> orderItems) {
        try {
            // 注意：由于相关类不存在，暂时注释掉邮件发送功能
            // 如果需要恢复此功能，需要重新实现相关的邮件发送逻辑
            log.info("快递订单创建成功，订单号: {}, 运单号: {}, 承运商: {}", 
                    order.getOrderNo(), expressOrderVO.getTransNo(), expressOrderVO.getLpName());
        } catch (Exception e) {
            log.error("send Email exception:", e);
        }
    }

    @Override
    @Transactional
    public void cancelExpressOrder(OrderCancelExpressDTO orderCancelExpressDTO) {
        // 当前登录CA
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        // 1. 校验订单 处于待收货
        OrderPO order = orderBizService.getOrderByCA(orderCancelExpressDTO.getOrderId(), wecomSession);
        if (!OrderStatusEnum.TO_RECEIVE.getCode().equals(order.getStatus())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_STATUS_CANNOT_CANCEL_EXPRESS);
        }
        // 2. 更新订单状态
        order.setStatus(OrderStatusEnum.TO_DELIVER.getCode());
        orderMapper.updateById(order);
        // 3. 调用Express进行取消
        // 注意：由于相关类不存在，暂时注释掉快递取消功能
        // 如果需要恢复此功能，需要重新实现相关的快递取消逻辑
        log.info("快递订单取消请求，订单号: {}, 运单号: {}, 承运商: {}", 
                order.getOrderNo(), order.getLogisticsNo(), order.getLogisticsCompany());
    }

    @Override
    @Transactional
    public void receiptOrder(OrderReceiptDTO receiptDTO) {
        // 获取当前用户 unionId
        WechatSessionInfoDTO wechatSession = SessionUtils.getWechatSession();
        // 查询订单
        OrderPO order = orderBizService.getOrderByCustomer(receiptDTO.getOrderId(), wechatSession);

        // 校验订单状态
        if (order.getStatus().intValue() == OrderStatusEnum.COMPLETED.getCode()) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_IS_COMPLETE);
        }
        if (order.getStatus().intValue() != OrderStatusEnum.TO_RECEIVE.getCode()) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_SHIPPED);
        }

        // 更新订单状态
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setReceiveTime(ZonedDateTime.now());
        order.setReceiveType(ReceiveType.SELF.getCode());
        orderMapper.updateById(order);

        // 记录订单日志
        OrderLogPO orderLog = OrderLogPO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(OrderStatusEnum.COMPLETED.getCode())
                .statusDesc(OrderStatusEnum.COMPLETED.getMsg())
                .type(OrderLogTypeEnum.CONFIRM_RECEIPT.getCode())
                .typeDesc(OrderLogTypeEnum.CONFIRM_RECEIPT.getMsg())
                .build();
        orderLogMapper.insert(orderLog);

        // 发送通知
        notificationBizService.sendCustomerConfirmedReceiptMsg(order);
    }

    @Override
    public ExpressOrderVO getOrderRouteInfo(Long orderId) {
        // 1. 查询订单
        OrderPO orderPO = orderMapper.selectById(orderId);
        if (orderPO == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }

        // 2. 权限校验
        WechatSessionInfoDTO wechatSession = SessionUtils.getWechatSession();
        if (wechatSession != null && !wechatSession.getUnionid().equals(orderPO.getCustomerUnionid())) {
            throw new ServiceException(RemotesalesErrorCode.NO_PERMISSION);
        }
        // 3. 构建物流查询请求
        R<ExpressOrderVO> expressOrderVOR = expressFeignClient.get(orderPO.getOrderNo(), orderPO.getLogisticsCompanyCode(), orderPO.getLogisticsNo(), orderPO.getCustomerMobile());
        if (!BasicErrorCode.SUCCESS.getCode().equals(expressOrderVOR.getCode())) {
            throw new ServiceException(expressOrderVOR.getCode(), expressOrderVOR.getMessage());
        }
        return expressOrderVOR.getData();
    }

    @Override
    public String getFaceToFaceQrCode(Long orderId) {
        WechatSessionInfoDTO wechatSession = SessionUtils.getWechatSession();
        OrderPO order = orderBizService.getOrderByCustomer(orderId, wechatSession);
        if (!OrderStatusEnum.TO_PICKUP.getCode().equals(order.getStatus())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_TO_PICKUP);
        }
        if (!DeliveryMethodEnum.SELF_PICKUP.getCode().equals(order.getDeliveryMethod())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_DELIVER_METHOD_NOT_SELF_PICKUP);
        }
        ConfigContentVO brandAllConfigContent = configCenterClient.getBrandAllConfigContent();
        Duration duration = Duration.ofSeconds(ObjectUtils.firstNonNull(brandAllConfigContent.getQrExpiry().getExpiryTime(), RSConstants.QR_CODE_EXPIRE_SECONDS));
        String code = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(RSConstants.ORDER_QR_CODE_CACHE_KEY + code, order.getId().toString(), duration);
        return code;
    }

    @Override
    @Transactional
    public void scanFaceToFaceQrCode(String code) {
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        String orderIdStr = stringRedisTemplate.opsForValue().get(RSConstants.ORDER_QR_CODE_CACHE_KEY + code);
        if (StringUtils.isBlank(orderIdStr)) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_QR_CODE_EXPIRED);
        }
        log.info("request scanFaceToFaceQrCode params:{}", orderIdStr);
        OrderPO order = orderMapper.selectById(orderIdStr);
        if (!OrderStatusEnum.TO_PICKUP.getCode().equals(order.getStatus())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_TO_PICKUP);
        }
        if (!DeliveryMethodEnum.SELF_PICKUP.getCode().equals(order.getDeliveryMethod())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_DELIVER_METHOD_NOT_SELF_PICKUP);
        }
        order.setStatus(OrderStatusEnum.TO_RECEIVE.getCode());
        order.setReceiveTime(ZonedDateTime.now());
        order.setReceiveType(ReceiveType.SELF.getCode());
        order.setDeliveryTime(ZonedDateTime.now());
        order.setPickupCaName(wecomSession.getName());
        order.setPickupCaWwid(wecomSession.getUserId());
        order.setPickupTime(ZonedDateTime.now());
        orderMapper.updateById(order);
        if (OrderStatusEnum.TO_RECEIVE.getCode().equals(order.getStatus())) {
            ConfigContentVO.PickupAutoConfirm pickupAutoConfirm = configCenterService.getBrandAllConfigContent().getPickupAutoConfirm();
            if (Objects.nonNull(pickupAutoConfirm) && Boolean.TRUE.equals(pickupAutoConfirm.getEnabled())) {
                ZonedDateTime zonedDateTime = ZonedDateTime.now().plusSeconds(pickupAutoConfirm.getAutoTime());
                EasyScheduler.createJob(SchedulerBizService.ORDER_RECEIPT_AUTO_CONFIRM, zonedDateTime, new Object[]{order.getId(), order.getDeliveryMethod(),order.getTenantId()});

            }
        }
        // 记录订单日志
        OrderLogPO orderLog = OrderLogPO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(OrderStatusEnum.COMPLETED.getCode())
                .statusDesc(OrderStatusEnum.COMPLETED.getMsg())
                .type(OrderLogTypeEnum.PICKUP_SCAN.getCode())
                .typeDesc(OrderLogTypeEnum.PICKUP_SCAN.getMsg())
                .build();
        orderLogMapper.insert(orderLog);
    }

    @Override
    @Transactional
    public void applyBill(OrderBillDTO billDTO) {
        WechatSessionInfoDTO wechatSession = SessionUtils.getWechatSession();
        OrderPO order = orderBizService.getOrderByCustomer(billDTO.getOrderId(), wechatSession);
        order.setBill(billDTO.getBill());
        orderMapper.updateById(order);
        saveOrderLog(order, Objects.requireNonNull(BaseEnums.getByCode(order.getStatus(), OrderStatusEnum.class)), OrderLogTypeEnum.APPLY_INVOICE);

    }

    @Override
    @Transactional
    public void auditAdjustPrice(OrderAdjustPriceAuditDTO adjustPriceAuditDTO) {
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        if (!wecomSession.getCaPosition().equals(RoleTypeConstants.STORE_MANAGER)) {
            throw new ServiceException(RemotesalesErrorCode.NO_PERMISSION);
        }
        OrderAdjustPricePO orderAdjustPricePO = orderAdjustPriceMapper.selectById(adjustPriceAuditDTO.getOrderAdjustPriceId());
        if (orderAdjustPricePO == null || !orderAdjustPricePO.getStoreId().equals(wecomSession.getStoreId())) {
            throw new ServiceException(RemotesalesErrorCode.PARAMERROR);
        }
        orderAdjustPricePO.setStatus(adjustPriceAuditDTO.getStatus());
        orderAdjustPricePO.setRejectRemark(adjustPriceAuditDTO.getRejectRemark());
        orderAdjustPricePO.setAuditorName(wecomSession.getName());
        orderAdjustPricePO.setAuditorWwid(wecomSession.getUserId());
        orderAdjustPriceMapper.updateById(orderAdjustPricePO);
        OrderPO orderPO = orderMapper.selectById(orderAdjustPricePO.getOrderId());
        if (orderPO.getPayStatus().equals(PayStatusEnum.PAID.getCode())) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_PAID_ORDER_CANNOT_ADJUST_PRICE);
        }
        OrderItemPO orderItemPO = orderItemMapper.selectById(orderAdjustPricePO.getOrderItemId());
        boolean approved = orderAdjustPricePO.getStatus() == OrderAdjustPriceStatusEnum.APPROVED.getCode().intValue();
        if (approved) {
            orderItemPO.setPrice(orderAdjustPricePO.getPrice());
            orderItemMapper.updateById(orderItemPO);
            List<OrderItemPO> orderItemPOS = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItemPO>().eq(OrderItemPO::getOrderId, orderAdjustPricePO.getOrderId()));
            BigDecimal newAmount = orderItemPOS.stream().map(item -> item.getPrice().multiply(new BigDecimal(item.getQty()))).reduce(BigDecimal.ZERO, BigDecimal::add);
            orderPO.setAmount(newAmount);
            orderMapper.updateById(orderPO);
        }
        // 调价审批通知
        notificationBizService.sendPriceAdjustmentResultMsg(orderPO, orderItemPO, orderAdjustPricePO, wecomSession.getName(), LocalDateTime.now(), approved, adjustPriceAuditDTO.getRejectRemark());
        saveOrderLog(orderPO, Objects.requireNonNull(BaseEnums.getByCode(orderPO.getStatus(), OrderStatusEnum.class)),approved?OrderLogTypeEnum.APPLY_ADJUST_APPROVE:OrderLogTypeEnum.APPLY_ADJUST_REJECT);
    }

    @Override
    public void applyAdjustPrice(OrderAdjustPriceApplyDTO adjustPriceApplyDTO) {
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        OrderItemPO orderItemPO = orderItemMapper.selectById(adjustPriceApplyDTO.getOrderItemId());
        if (orderItemPO == null) {
            throw new ServiceException(RemotesalesErrorCode.PARAMERROR);
        }
        // 校验权限
        OrderPO order = orderBizService.getOrderByCA(orderItemPO.getOrderId(), wecomSession);
        Long pendingCount = orderAdjustPriceMapper.selectCount(
                new LambdaQueryWrapper<OrderAdjustPricePO>()
                        .eq(OrderAdjustPricePO::getOrderItemId, orderItemPO.getId())
                        .eq(OrderAdjustPricePO::getStatus, OrderAdjustPriceStatusEnum.PENDING.getCode()));
        if (pendingCount > 0) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_ADJUST_PRICE_EXIST_PENDING);
        }
        // 创建调价单
        OrderAdjustPricePO existAdjustPricePO = orderAdjustPriceMapper.selectOne(new LambdaQueryWrapper<OrderAdjustPricePO>()
                .eq(OrderAdjustPricePO::getOrderItemId, orderItemPO.getId())
                .orderByDesc(OrderAdjustPricePO::getId)
                .last("limit 1"));
        if (existAdjustPricePO == null) {
            OrderAdjustPricePO orderAdjustPricePO = new OrderAdjustPricePO();
            orderAdjustPricePO.setOrderId(order.getId());
            orderAdjustPricePO.setPrice(adjustPriceApplyDTO.getPrice());
            orderAdjustPricePO.setOriginalPrice(orderItemPO.getOriginalPrice());
            orderAdjustPricePO.setOrderItemId(orderItemPO.getId());
            orderAdjustPricePO.setStoreId(order.getStoreId());
            orderAdjustPricePO.setQty(orderItemPO.getQty());
            orderAdjustPricePO.setStatus(OrderAdjustPriceStatusEnum.PENDING.getCode());
            orderAdjustPricePO.setInnerRemark(adjustPriceApplyDTO.getInnerRemark());
            orderAdjustPriceMapper.insert(orderAdjustPricePO);
            // 调价申请通知
            notificationBizService.sendPriceAdjustmentAudit(wecomSession.getName() + ADJUST_TEXT, order, orderItemPO, orderAdjustPricePO, wecomSession.getCurrentStore().getManagerWwid());
        } else {
            existAdjustPricePO.setPrice(adjustPriceApplyDTO.getPrice());
            existAdjustPricePO.setStatus(OrderAdjustPriceStatusEnum.PENDING.getCode());
            existAdjustPricePO.setInnerRemark(adjustPriceApplyDTO.getInnerRemark());
            orderAdjustPriceMapper.updateById(existAdjustPricePO);
            // 调价申请通知
            notificationBizService.sendPriceAdjustmentAudit(wecomSession.getName() + ADJUST_TEXT, order, orderItemPO, existAdjustPricePO, wecomSession.getCurrentStore().getManagerWwid());
        }
        saveOrderLog(order, Objects.requireNonNull(BaseEnums.getByCode(order.getStatus(), OrderStatusEnum.class)),OrderLogTypeEnum.APPLY_ADJUST);
    }

    @Override
    public CreatePayUrlVo createSelfPayUrl(CreateFriendPayLinkDTO req) {
        WechatSessionInfoDTO wechatSession = SessionUtils.getWechatSession();
        OrderPO orderPO = orderMapper.selectOne(new LambdaQueryWrapper<OrderPO>().eq(OrderPO::getOrderNo, req.getOrderNo()));
        if (orderPO == null || orderPO.getStatus() == OrderStatusEnum.TO_FORWARD.getCode().intValue()) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }

        List<OrderItemPO> orderItemPOS = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItemPO>().eq(OrderItemPO::getOrderId, orderPO.getId()));

        TransactionCreateDTO transactionCreateDTO = new TransactionCreateDTO();
        transactionCreateDTO.setOrderNo(req.getOrderNo());
        transactionCreateDTO.setCurrencyCode(CurrencyCode.CNY);
        transactionCreateDTO.setAmount(orderPO.getAmount());
        transactionCreateDTO.setOpenId(wechatSession.getOpenid());
        transactionCreateDTO.setItems(orderItemPOS.stream().map(po -> {
            TransactionCreateDTO.Item item = new TransactionCreateDTO.Item();
            item.setName(po.getProductName());
            item.setPrice(po.getPrice());
            item.setQuantity(new BigDecimal(po.getQty()));
            item.setSku(po.getSku());
            return item;
        }).toList());
        R<Object> r = transactionFeignClient.prepay(transactionCreateDTO);
        if (!BasicErrorCode.SUCCESS.getCode().equals(r.getCode())) {
            throw new ServiceException(r.getCode(), r.getMessage());
        }

        orderPO.setPayCustomerOpenid(wechatSession.getOpenid());
        orderPO.setPayCustomerUnionid(wechatSession.getUnionid());
        orderMapper.updateById(orderPO);
        CreatePayUrlVo vo = new CreatePayUrlVo();
        OrderVO ordersVo = new OrderVO();
        BeanUtils.copyProperties(orderPO, ordersVo);
        vo.setOrder(ordersVo);
        vo.setPrepay(r.getData());
        return vo;
    }


    @Override
    public PageResult<SystemOrderVO> getSystemOrderList(String keyword, String storeId, Integer receiveType, Integer deliveryMethod, Integer status, Integer payStatus, Boolean isRefund, Integer refundStatus, ZonedDateTime startTime, ZonedDateTime endTime, Integer pageNum, Integer pageSize) {
        // 1. 构建分页对象
        Page<OrderPO> page = new Page<>(pageNum, pageSize);
        // 2. 构建查询条件
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<>();

        // 收货方式
        wrapper.eq(Objects.nonNull(receiveType), OrderPO::getReceiveType, receiveType)
                // 配送方式
                .eq(Objects.nonNull(deliveryMethod), OrderPO::getDeliveryMethod, deliveryMethod)
                // 店铺ID
                .eq(StringUtils.isNotBlank(storeId), OrderPO::getStoreId, storeId)
                // 订单状态
                .eq(Objects.nonNull(status), OrderPO::getStatus, status)
                //支付状态
                .eq(Objects.nonNull(payStatus), OrderPO::getPayStatus, payStatus)
                // 时间范围
                .ge(Objects.nonNull(startTime), OrderPO::getCreatedTime, startTime)
                .le(Objects.nonNull(endTime), OrderPO::getCreatedTime, endTime);
        // 是否退款
        if (Boolean.TRUE.equals(isRefund)) {
            wrapper.gt(OrderPO::getTotalRefundCount, 0);
        }
        //退款方式
        if (Objects.nonNull(refundStatus)) {
            if (RefundStatusVOEnum.ALL_REFUND.getCode().equals(refundStatus)) {
                wrapper.apply("total_refund >= pay_amount and pay_amount !=0");
            } else if (RefundStatusVOEnum.PARTIAL_REFUND.getCode().equals(refundStatus)) {
                wrapper.gt(OrderPO::getTotalRefundCount, 0).apply("total_refund < pay_amount");
            } else if (RefundStatusVOEnum.NOT_REFUND.getCode().equals(refundStatus)) {
                wrapper.eq(OrderPO::getTotalRefundCount, 0);
            }
        }
        // 关键字搜索
        wrapper.and(StringUtils.isNotBlank(keyword), w -> w
                .like(OrderPO::getOrderNo, keyword)
                .or().like(OrderPO::getCaName, keyword)

        );
        // 按创建时间倒序排序
        wrapper.orderByDesc(OrderPO::getId);

        // 3. 查询订单分页
        Page<OrderPO> orderPage = orderMapper.selectPage(page, wrapper);
        List<OrderPO> orders = orderPage.getRecords();
        if (orders.isEmpty()) {
            return PageResult.<SystemOrderVO>builder()
                    .totalCount(0)
                    .totalPages(0)
                    .pageNumber(pageNum)
                    .pageSize(pageSize)
                    .hasMore(false)
                    .data(new ArrayList<>())
                    .build();
        }

        // 4. 组装 VO
        List<SystemOrderVO> voList = orders.stream().map(SystemOrderVO::of).toList();
        // 5. 返回分页结果
        boolean hasMore = orderPage.getCurrent() < orderPage.getPages();
        return PageResult.<SystemOrderVO>builder()
                .totalCount(orderPage.getTotal())
                .totalPages(orderPage.getPages())
                .pageNumber((int) orderPage.getCurrent())
                .pageSize((int) orderPage.getSize())
                .hasMore(hasMore)
                .data(voList)
                .build();
    }

    @Override
    public SystemOrderDetailVO getSystemOrderDetail(String orderNo) {
        // 1. 查询订单
        OrderPO orderPO = orderMapper.selectOne(new LambdaQueryWrapper<OrderPO>().eq(OrderPO::getOrderNo, orderNo));
        if (orderPO == null) {
            throw new ServiceException(RemotesalesErrorCode.ORDER_NOT_EXIST);
        }

        // 2. 组装订单详情
        SystemOrderDetailVO orderDetailVO = SystemOrderDetailVO.of(orderPO);

        Map<Long, List<OrderItemPO>> itemMap = getOrderItems(List.of(orderPO.getId()));
        if (CollectionUtils.isNotEmpty(itemMap)) {
            orderDetailVO.setOrderItems(itemMap.get(orderPO.getId()).stream().map(OrderItemVO::of).toList());
        }

        // 3. 组装提货店铺信息
        if (StringUtils.isNotEmpty(orderPO.getPickupStoreId())) {
            R<LocationVO> responseVo = locationFeignClient.detail(orderPO.getPickupStoreId());
            LocationVO data = responseVo.getData();
            if (data != null) {
                OrderPickupStoreVO pickupStoreVO = new OrderPickupStoreVO();
                BeanUtils.copyProperties(data, pickupStoreVO);
                orderDetailVO.setPickupStoreVo(pickupStoreVO);
            }
        }
        // 4. 组装物流信息
        if (Objects.equals(DeliveryMethodEnum.LOGISTICS.getCode(), orderPO.getDeliveryMethod())) {
            ExpressNumberVO expressNumber = new ExpressNumberVO();
            BeanUtils.copyProperties(orderPO, expressNumber);
            orderDetailVO.setExpressNumber(expressNumber);
        }


        // 5. 组装支付信息
        if (StringUtils.isNotEmpty(orderPO.getPayMethod())) {
            PaymentVO paymentVO = new PaymentVO();
            paymentVO.setPayType(BaseEnums.getMsgByCode(orderPO.getPayType(),PayTypeEnum.class));
            paymentVO.setPayMethod(orderPO.getPayMethod());
            paymentVO.setPayDate(orderPO.getPayTime());
            paymentVO.setPaySeqNo(orderPO.getPayNo());
            paymentVO.setPayAmount(orderPO.getPayAmount());
            orderDetailVO.setPaymentVO(paymentVO);
        }

        // 6. 订单日志
        List<OrderLogPO> orderLogPOS = orderLogMapper.selectList(
                new LambdaQueryWrapper<OrderLogPO>()
                        .eq(OrderLogPO::getOrderNo, orderNo)
                        .orderByDesc(OrderLogPO::getId));
        List<OrderLogVO> orderLogVOS = orderLogPOS.stream().map(OrderLogVO::of).toList();
        orderDetailVO.setOrderLogs(orderLogVOS);

        // 7. 支付记录
        List<OrderPayLogPO> orderPayLogPOS = orderPayLogMapper.selectList(
                new LambdaQueryWrapper<OrderPayLogPO>()
                        .eq(OrderPayLogPO::getOrderId, orderPO.getId()));
        List<OrderPayLogVO> orderPayLogVOS = orderPayLogPOS.stream().map(OrderPayLogVO::of).toList();
        orderDetailVO.setOrderPayLogs(orderPayLogVOS);

        return orderDetailVO;
    }

    private OrderItemPO assembleSubmitOrderItem(OrderItemSubmitDTO itemDTO, WecomSessionInfoDTO wecomSession) {
        OrderItemPO item = new OrderItemPO();
        item.setSku(itemDTO.getSku());
        item.setQty(itemDTO.getQty());
        item.setRemark(itemDTO.getRemark());
        item.setInnerRemark(itemDTO.getInnerRemark());
        // 从IODS再次查询商品信息
        ProductVo itemDetail = productClient.getItemDetail(itemDTO.getSku(), wecomSession.getStoreCode());
        item.setSmc(itemDetail.getSmc());
        item.setProductName(itemDetail.getProductName());
        item.setProductDesc(itemDetail.getDescription());
        item.setProductSize(itemDetail.getSize());
        item.setProductPic(itemDetail.getPic());
        item.setColor(itemDetail.getColor());
        item.setColorCode(itemDetail.getColorCode());
        item.setPrice(itemDetail.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
        item.setOriginalPrice(itemDetail.getOriginalPrice() != null ? itemDetail.getOriginalPrice() : BigDecimal.ZERO);
        item.setReturnInSevenDays(true);
        item.setDepartmentCode(itemDetail.getDepartmentCode());
        item.setDepartmentName(itemDetail.getDepartmentName());
        item.setDepartmentGroupCode(itemDetail.getDepartmentGroupCode());
        item.setDepartmentGroupName(itemDetail.getDepartmentGroupName());
        item.setMarkDown(itemDetail.getMarkDown());
        return item;
    }

    private void tryQueryPayResult(OrderPO orderPO) {
        if (OrderStatusEnum.TO_PAY.getCode().equals(orderPO.getStatus())) {
            try {
                // TODO payment
            } catch (Exception e) {
                log.error("[ICBC] query icbc order:{} error", orderPO.getOrderNo(), e);
            }
        }
    }

    /**
     * 组装订单详情
     *
     * @param orderPO
     * @return
     */
    private OrderDetailVO assembleOrderDetail(OrderPO orderPO) {
        OrderDetailVO orderDetailVO = OrderDetailVO.of(orderPO);
        // 组装SKU信息
        this.assembleOrderItems(List.of(orderDetailVO));
        // 组装调价日志
        List<OrderAdjustPricePO> adjustPricePOs = orderAdjustPriceMapper.selectList(new LambdaQueryWrapper<OrderAdjustPricePO>().eq(OrderAdjustPricePO::getOrderId, orderPO.getId()));
        List<OrderAdjustPriceVO> adjustPriceVOS = adjustPricePOs.stream().map(OrderAdjustPriceVO::of).toList();
        orderDetailVO.setAdjustPriceVos(adjustPriceVOS);
        // 更新SKU调价
        List<OrderItemVO> orderItems = orderDetailVO.getOrderItems();
        Map<Long, List<OrderAdjustPricePO>> adjustMap = adjustPricePOs.stream().collect(Collectors.groupingBy(OrderAdjustPricePO::getOrderItemId));
        for (OrderItemVO orderItem : orderItems) {
            List<OrderAdjustPricePO> orderAdjustPricePOS = adjustMap.get(orderItem.getId());
            orderItem.setAdjustedPrice(orderItem.getPrice());
            if (!CollectionUtils.isEmpty(orderAdjustPricePOS)) {
                // 取最后一个申请的
                BigDecimal adjustedPrice = orderAdjustPricePOS.stream()
                        .filter(a -> !a.getStatus().equals(OrderAdjustPriceStatusEnum.REJECTED.getCode())) // 忽略掉已经拒绝的
                        .sorted((a1, a2) -> a2.getId().compareTo(a1.getId()))
                        .map(OrderAdjustPricePO::getPrice)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
                if (!BigDecimal.ZERO.equals(adjustedPrice)) {
                    orderItem.setAdjustedPrice(adjustedPrice);
                }
            }
        }
        // 调价后总价
        BigDecimal adjustedOrderAmount = orderItems
                .stream()
                .filter(item -> item.getAdjustedPrice() != null)
                .map(item -> item.getAdjustedPrice().multiply(new BigDecimal(item.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderDetailVO.setAdjustedAmount(adjustedOrderAmount);
        // 组装退款日志
        List<OrderRefundPO> refundPOs = orderRefundMapper.selectList(new LambdaQueryWrapper<OrderRefundPO>().eq(OrderRefundPO::getOrderId, orderPO.getId()));
        List<OrderRefundVO> refundVOS = refundPOs.stream().map(OrderRefundVO::of).toList();
        orderDetailVO.setOrderRefundVos(refundVOS);
        // Store HUB 组装提货店铺信息 （选择自提 || 未选择提货地址时 || 订单未支付)
        if (orderDetailVO.pickupStoreShow()) {
            R<LocationVO> responseVo = locationFeignClient.detail(StringUtils.firstNonBlank(orderPO.getPickupStoreId(), orderPO.getStoreId()));
            LocationVO data = responseVo.getData();
            if (data != null) {
                OrderPickupStoreVO pickupStoreVO = new OrderPickupStoreVO();
                BeanUtils.copyProperties(data, pickupStoreVO);
                orderDetailVO.setPickupStoreVo(pickupStoreVO);
            }
        }
        // 组装支付记录
        List<OrderPayLogPO> orderPayLogPOS = orderPayLogMapper.selectList(new LambdaQueryWrapper<OrderPayLogPO>().eq(OrderPayLogPO::getOrderId, orderPO.getId()));
        List<OrderPayLogVO> orderPayLogVOS = orderPayLogPOS.stream().map(OrderPayLogVO::of).toList();
        orderDetailVO.setOrderPayLogVos(orderPayLogVOS);
        // 转化格式（无数据库操作）
        ConfigContentVO brandAllConfigContent = configCenterService.getBrandAllConfigContent();
        orderDetailVO.convertFormat(brandAllConfigContent.getOrderExpiry().getExpiryTime());
        return orderDetailVO;
    }


    /**
     * 批量组装OrderItems
     *
     * @param orderItemsOwnerVOS
     */
    private void assembleOrderItems(List<? extends OrderItemsOwnerVO> orderItemsOwnerVOS) {
        if (CollectionUtils.isEmpty(orderItemsOwnerVOS)) {
            return;
        }
        List<Long> orderIds = orderItemsOwnerVOS.stream().map(OrderItemsOwnerVO::getId).toList();
        Map<Long, List<OrderItemPO>> orderItemsMap = getOrderItems(orderIds);
        for (OrderItemsOwnerVO orderItemsOwnerVO : orderItemsOwnerVOS) {
            orderItemsOwnerVO.setOrderItems(orderItemsMap.getOrDefault(orderItemsOwnerVO.getId(), new ArrayList<>()).stream().map(OrderItemVO::of).toList());
        }
    }

    private Map<Long, List<OrderItemPO>> getOrderItems(List<Long> orderIds) {

        LambdaQueryWrapper<OrderItemPO> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItemPO::getOrderId, orderIds);
        List<OrderItemPO> allItems = orderItemMapper.selectList(itemWrapper);
        return allItems.stream().collect(Collectors.groupingBy(OrderItemPO::getOrderId));

    }

    @Override
    public void exportSystemOrderList(String keyword, String storeId, Integer receiveType, Integer deliveryMethod, Integer status, Integer payStatus, Boolean isRefund, Integer refundStatus, ZonedDateTime startTime, ZonedDateTime endTime, HttpServletResponse response) {
        // 1. 构建查询条件
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<OrderPO>()
                .eq(Objects.nonNull(receiveType), OrderPO::getReceiveType, receiveType)
                // 配送方式
                .eq(Objects.nonNull(deliveryMethod), OrderPO::getDeliveryMethod, deliveryMethod)
                // 店铺ID
                .eq(StringUtils.isNotBlank(storeId), OrderPO::getStoreId, storeId)
                // 订单状态
                .eq(Objects.nonNull(status), OrderPO::getStatus, status)
                //支付状态
                .eq(Objects.nonNull(payStatus), OrderPO::getPayStatus, payStatus)
                // 时间范围
                .ge(Objects.nonNull(startTime), OrderPO::getCreatedTime, startTime)
                .le(Objects.nonNull(endTime), OrderPO::getCreatedTime, endTime);
        // 是否退款
        if (Boolean.TRUE.equals(isRefund)) {
            wrapper.gt(OrderPO::getTotalRefundCount, 0);
        }
        //退款方式
        if (Objects.nonNull(refundStatus)) {
            if (RefundStatusVOEnum.ALL_REFUND.getCode().equals(refundStatus)) {
                wrapper.apply("total_refund >= pay_amount");
            } else if (RefundStatusVOEnum.PARTIAL_REFUND.getCode().equals(refundStatus)) {
                wrapper.gt(OrderPO::getTotalRefundCount, 0).apply("total_refund < pay_amount");
            } else if (RefundStatusVOEnum.NOT_REFUND.getCode().equals(refundStatus)) {
                wrapper.eq(OrderPO::getTotalRefundCount, 0);
            }
        }
        // 关键字搜索
        wrapper.and(StringUtils.isNotBlank(keyword), w -> w
                .like(OrderPO::getOrderNo, keyword)
                .or().like(OrderPO::getCaName, keyword)

        );

        // 2. 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("订单列表");

            // 3. 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"下单时间", "订单号", "员工工号", "员工姓名", "门店", "客户昵称", "订单状态",
                    "订单金额", "付款金额", "配送方式", "物流公司", "运单编号", "POS单号", "小票单号"
                    ,"XStoreId", "SKU", "条形码", "商品名称", "描述", "备注（内部可见）", "备注（客人可见）", "商品价格", "销售价格", "件数"

            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 4. 分页查询并填充数据
            int pageSize = 1000; // 每页1000条数据
            int pageNum = 1;
            int rowNum = 1;
            boolean hasMore = true;

            while (hasMore) {
                // 4.1 分页查询订单
                Page<OrderPO> page = new Page<>(pageNum, pageSize);
                Page<OrderItemExportVO> orderPage = orderMapper.selectOrderAndItemByPage(page, wrapper);
                List<OrderItemExportVO> orders = orderPage.getRecords();

                if (orders.isEmpty()) {
                    break;
                }

                // 4.2 填充数据
                for (OrderItemExportVO order : orders) {
                    rowNum = orderListRowData(order, sheet, rowNum);
                }

                // 4.3 判断是否还有更多数据
                hasMore = orderPage.getCurrent() < orderPage.getPages();
                pageNum++;
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 25*256);
            }
            // 5. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=orders_" + DateUtils.localDateTimeToString(LocalDateTime.now(), "yyyy_MM_dd_HH_mm_ss") + ".xlsx");

            // 6. 写入响应流
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("导出订单列表失败", e);
            throw new ServiceException(RemotesalesErrorCode.EXPORT_FAILED);
        }
    }

    private static int orderListRowData(OrderItemExportVO order, Sheet sheet, int rowNum) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(Objects.isNull(order.getCreatedTime())?"":DateTimeFormatter.ofPattern(DATE_FORMAT).format(order.getCreatedTime()));
        row.createCell(1).setCellValue(order.getOrderNo());
        row.createCell(2).setCellValue(order.getCaWwid());
        row.createCell(3).setCellValue(order.getCaName());
        row.createCell(4).setCellValue(order.getStoreName());
        row.createCell(5).setCellValue(order.getCustomerNickname());
        row.createCell(6).setCellValue(BaseEnums.getMsgByCode(order.getStatus(), OrderStatusEnum.class));
        row.createCell(7).setCellValue(Objects.isNull(order.getAmount())?"": order.getAmount().toString());
        row.createCell(8).setCellValue(Objects.isNull(order.getPayAmount())?"": order.getPayAmount().toString());
        row.createCell(9).setCellValue(BaseEnums.getMsgByCode(order.getDeliveryMethod(), DeliveryMethodEnum.class));
        row.createCell(10).setCellValue(order.getLogisticsCompany());
        row.createCell(11).setCellValue(order.getLogisticsNo());
        row.createCell(12).setCellValue(order.getPosNumber());
        row.createCell(13).setCellValue(order.getReceiptNumber());

        row.createCell(14).setCellValue(order.getXStoreId());
        row.createCell(15).setCellValue(order.getSku());
        row.createCell(16).setCellValue(order.getSku());
        row.createCell(17).setCellValue(order.getProductName());
        row.createCell(18).setCellValue(order.getProductDesc());
        row.createCell(19).setCellValue(order.getInnerRemark());
        row.createCell(20).setCellValue(order.getRemark());
        row.createCell(21).setCellValue(Objects.isNull(order.getOriginalPrice())?"":order.getOriginalPrice().setScale(2,RoundingMode.HALF_UP).toString());
        row.createCell(22).setCellValue(Objects.isNull(order.getPrice())?"":order.getPrice().setScale(2,RoundingMode.HALF_UP).toString());
        row.createCell(23).setCellValue(Objects.isNull(order.getQty())?"":order.getQty().toString());


        return rowNum;
    }

    @Override
    public PageResult<SystemRefundOrderVO> getSystemRefundOrderList(Integer refundStatus, Integer approveStatus, Integer orderStatus, Integer pageNum, Integer pageSize) {
        Page<SystemRefundOrderVO> pageParams = new Page<>(pageNum, pageSize);
        Map<String, Object> params = new HashMap<>();
        params.put("refundStatus", refundStatus);
        params.put("approveStatus", approveStatus);
        params.put("orderStatus", orderStatus);
        IPage<SystemRefundOrderVO> result = orderRefundMapper.getSystemRefundOrderList(pageParams, params);

        boolean hasMore = result.getCurrent() < result.getPages();
        return PageResult.<SystemRefundOrderVO>builder()
                .totalCount(result.getTotal())
                .totalPages(result.getPages())
                .pageNumber((int) result.getCurrent())
                .pageSize((int) result.getSize())
                .hasMore(hasMore)
                .data(result.getRecords())
                .build();
    }

    @Override
    public void getSystemRefundOrderExport(Integer refundStatus, Integer approveStatus, Integer orderStatus, HttpServletResponse response) {

        // 2. 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook()) {

            CellStyle cellStyleTime = workbook.createCellStyle();
            DataFormat datetimeFormat = workbook.createDataFormat();
            cellStyleTime.setDataFormat(datetimeFormat.getFormat(DATE_FORMAT));

            Sheet sheet = workbook.createSheet("退款列表");
            Map<String, Object> params = new HashMap<>();
            params.put("refundStatus", refundStatus);
            params.put("approveStatus", approveStatus);
            params.put("orderStatus", orderStatus);
            // 3. 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"订单号", "下单时间", "员工工号", "员工姓名", "门店", "客人昵称", "POS单号", "小票单号",
                    "申请人", "退款方式", "流水号", "退款时间", "退款金额", "剩余金额", "申请时间", "审核时间", "退款理由", "审核人", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                sheet.autoSizeColumn(i);
            }

            // 4. 分页查询并填充数据
            int pageSize = 1000; // 每页1000条数据
            int pageNum = 1;
            int rowNum = 1;
            boolean hasMore = true;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            while (hasMore) {
                // 4.1 分页查询订单
                Page<SystemRefundOrderVO> page = new Page<>(pageNum, pageSize);
                IPage<SystemRefundOrderVO> orderPage = orderRefundMapper.getSystemRefundOrderList(page, params);
                List<SystemRefundOrderVO> orders = orderPage.getRecords();

                if (orders.isEmpty()) {
                    break;
                }

                // 4.2 填充数据
                for (SystemRefundOrderVO order : orders) {
                    rowNum = createRowData(order, sheet, rowNum, dateTimeFormatter, cellStyleTime);

                }

                // 4.3 判断是否还有更多数据
                hasMore = orderPage.getCurrent() < orderPage.getPages();
                pageNum++;
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 25*256);
            }
            // 5. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=refund_orders_" + DateUtils.localDateTimeToString(LocalDateTime.now(), "yyyy_MM_dd_HH_mm_ss") + ".xlsx");

            // 6. 写入响应流
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("导出退款列表失败", e);
            throw new ServiceException(RemotesalesErrorCode.EXPORT_FAILED);
        }
    }

    private static int createRowData(SystemRefundOrderVO order, Sheet sheet, int rowNum, DateTimeFormatter dateTimeFormatter, CellStyle cellStyleTime) {
        Row row = sheet.createRow(rowNum++);

        row.createCell(0).setCellValue(order.getOrderNo());
        row.createCell(1).setCellValue(order.getOrderCreateTime());
        row.createCell(2).setCellValue(order.getCreatorWwid());
        row.createCell(3).setCellValue(order.getCreatorName());
        row.createCell(4).setCellValue(order.getStoreName());
        row.createCell(5).setCellValue(order.getCustomerNickname());
        row.createCell(6).setCellValue(order.getPosNumber());
        row.createCell(7).setCellValue(order.getReceiptNumber());
        row.createCell(8).setCellValue(order.getCreatorWwid());
        row.createCell(9).setCellValue(order.getPayMethod());
        row.createCell(10).setCellValue(order.getOriginalNo());
        Cell cellData = row.createCell(11);
        cellData.setCellStyle(cellStyleTime);
        cellData.setCellValue(order.getRefundTime() == null ? "" : dateTimeFormatter.format(order.getRefundTime()));
        row.createCell(12).setCellValue(order.getRefundAmount().setScale(2, RoundingMode.HALF_UP).toString());
        row.createCell(13).setCellValue(Objects.nonNull(order.getRemainingAmount()) ? order.getRemainingAmount().setScale(2, RoundingMode.HALF_UP).toString() : "");
        cellData = row.createCell(14);
        cellData.setCellStyle(cellStyleTime);
        cellData.setCellValue(Objects.isNull(order.getApproveTime()) ? "" : dateTimeFormatter.format(order.getRefundCreateTime()));
        cellData = row.createCell(15);
        cellData.setCellStyle(cellStyleTime);
        cellData.setCellValue(Objects.isNull(order.getApproveTime()) ? "" : dateTimeFormatter.format(order.getApproveTime()));
        row.createCell(16).setCellValue(order.getRefundComment());
        row.createCell(17).setCellValue(order.getApproveName());
        row.createCell(18).setCellValue(BaseEnums.getMsgByCode(order.getRefundStatus(), OrderRefundStatusEnum.class));
        return rowNum;
    }

}
