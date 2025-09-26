package com.dobbinsoft.gus.remotesales.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.common.utils.context.GenericRequestContextHolder;
import com.dobbinsoft.gus.common.utils.context.bo.TenantContext;
import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.client.configcenter.ConfigCenterClient;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.client.gus.location.LocationFeignClient;
import com.dobbinsoft.gus.remotesales.client.gus.location.model.LocationQueryDTO;
import com.dobbinsoft.gus.remotesales.client.gus.location.model.LocationStatus;
import com.dobbinsoft.gus.remotesales.client.gus.location.model.LocationType;
import com.dobbinsoft.gus.remotesales.client.gus.location.model.LocationVO;
import com.dobbinsoft.gus.remotesales.client.wecom.WeComAdapterClient;
import com.dobbinsoft.gus.remotesales.client.wecom.vo.*;
import com.dobbinsoft.gus.remotesales.data.constant.RoleTypeConstants;
import com.dobbinsoft.gus.remotesales.data.dto.auth.WecomAuthDTO;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.data.vo.JSSDKConfigVo;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.service.WecomAuthService;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import com.dobbinsoft.gus.remotesales.utils.SignUtil;
import com.dobbinsoft.gus.web.exception.ServiceException;
import com.dobbinsoft.gus.web.vo.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WecomAuthServiceImpl implements WecomAuthService {

    private static final String REDIS_CACHE_WECOM_USER_INFO = "CUS:WECOM:LOGIN:USER:INFO:";
    private static final String REDIS_CACHE_JS_TICKET_CACHE = "CUS:WECOM:JS_TICKET:";
    private static final String REDIS_CACHE_WECHAT_JS_TICKET_CACHE = "CUS:WECOM:WECHAT_JS_TICKET:";

    private static final Map<String, String> CONTRY_NAME_MAP = new HashMap<>();
    private static final long EXPIRES_CLOSE = 30;
    private static final String UNKNOWN = "unknown";

    static {
        CONTRY_NAME_MAP.put("CN", "中国大陆");
        CONTRY_NAME_MAP.put("TW", "台湾");
        CONTRY_NAME_MAP.put("MO", "澳门");
        CONTRY_NAME_MAP.put("HK", "香港");

    }

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private LocationFeignClient locationFeignClient;
    @Autowired
    private WeComAdapterClient weComAdapterClient;
    @Autowired
    private ConfigCenterClient configCenterClient;

    @Override
    public WecomSessionInfoDTO login(WecomAuthDTO wecomAuthDTO) {
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        WeComAuthTicket authTicket = weComAdapterClient.getAuthTicket(null, wecomAuthDTO.getCode());
        if (authTicket.getErrcode() != 0) {
            throw new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
        }
        WeComUserDetail userDetail = weComAdapterClient.getUserDetail(configContentVO.getBrand().getAgentId(), authTicket.getUserTicket());
        log.info("[Wecom login] userDetail:{}", userDetail);
        if (userDetail == null || StringUtils.isEmpty(userDetail.getUserid())) {
            log.warn("未获取到访问用户信息");
            throw new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
        }
        WeComUserInfo userInfo = weComAdapterClient.getUserInfo(configContentVO.getBrand().getAgentId(), userDetail.getUserid());
        WecomSessionInfoDTO beforeSession=getLoginSessionUserInfo(userDetail.getUserid());
        WecomSessionInfoDTO headerInfo = new WecomSessionInfoDTO();
        headerInfo.setUserId(userDetail.getUserid());
        headerInfo.setName(userInfo.getName());
        headerInfo.setCaAvatar(userDetail.getAvatar());
        headerInfo.setCaPosition(RoleTypeConstants.SALES);
        //兼容切换角色，如果session还存在保留上一次的 角色和店铺

        headerInfo.setStores(getAllStores().stream().map(v -> getSessionStore(v, userInfo, userDetail, headerInfo)).filter(Objects::nonNull).toList());
        String thisPosition=headerInfo.getCaPosition();
        if(Objects.nonNull(beforeSession)&&!RoleTypeConstants.AREA_MANAGER.equals(thisPosition)&&!RoleTypeConstants.AREA_MANAGER.equals(beforeSession.getCaPosition())) {
            log.info("wxlogin  save cache session caPosition : {} ", beforeSession.getCaPosition());
            switchUserInfo(headerInfo,beforeSession.getCurrentStoreId(),beforeSession.getCaPosition());
        }else{
            headerInfo.setCaPosition(thisPosition);
            //如果 为空则需要设置一个默认店, 区经角色在 convertStoreRoleData 已赋值
            if(StringUtils.isEmpty(headerInfo.getCurrentStoreId())){
                headerInfo.setCurrentStoreId(headerInfo.getStores().stream().findFirst().map(WecomSessionInfoDTO.Store::getStoreId).orElse(null));
            }

        }


        // 默认缓存1天，访问时自动续约至一天
        redisTemplate.opsForValue()
                .set(REDIS_CACHE_WECOM_USER_INFO.concat(userDetail.getUserid()), JsonUtil.convertToString(headerInfo), Duration.ofDays(1));
        return headerInfo;
    }

    private WecomSessionInfoDTO.Store getSessionStore(LocationVO v, WeComUserInfo userInfo, WeComUserDetail userDetail, WecomSessionInfoDTO headerInfo) {
        int[] department = userInfo.getDepartment();
        if (StringUtils.isEmpty(v.getWecomDeptId())) {
            return null;
        }
        List<String> wecomDepartmentIds = Arrays.asList(v.getWecomDeptId().split(","));
        if (CollectionUtils.isEmpty(wecomDepartmentIds)) {
            return null;
        }
        int[] intersection = Arrays.stream(department).filter(deptId -> wecomDepartmentIds.contains(deptId + "")).toArray();
        if (intersection.length == 0) {
            return null;
        }
        WecomSessionInfoDTO.Store store = new WecomSessionInfoDTO.Store();
        store.setWecomDeptIds(intersection);
        store.setStoreId(v.getId());
        store.setStoreCode(v.getCode());
        store.setStoreName(v.getName());
        store.setMobile(v.getMobile());
        store.setTelephone(v.getTelephone());
        if (StringUtils.isNotEmpty(v.getCountryCode())) {
            store.setCountry(CONTRY_NAME_MAP.get(v.getCountryCode()));
        }
        store.setProvince(v.getProvince());
        store.setCity(v.getCity());
        store.setDistrict(v.getDistrict());
        store.setAddress(v.getAddress());
        store.setZipcode(v.getPostalCode());
        convertStoreRoleData(v, store, userDetail, headerInfo);
        setStoreCaInfo(configCenterClient.getBrandAllConfigContent(),store,intersection);
        return store;
    }

    private static void convertStoreRoleData(LocationVO v, WecomSessionInfoDTO.Store store, WeComUserDetail weComEmployeeInfoResponse, WecomSessionInfoDTO headerInfo) {
        store.setIsManager(StringUtils.isNotEmpty(v.getLocationManagerEmail()) && v.getLocationManagerEmail().equals(weComEmployeeInfoResponse.getEmail()));
        store.setManagerEmail(v.getLocationManagerEmail());
        if (store.getIsManager()) {
            store.setManagerWwid(headerInfo.getUserId());
            if (store.getIsManager() && !RoleTypeConstants.AREA_MANAGER.equals(headerInfo.getCaPosition())) {
                headerInfo.setCaPosition(RoleTypeConstants.STORE_MANAGER);
            }
        }
        // 使用 LocationVO 中的区域信息
        if (StringUtils.isNotEmpty(v.getRegionCode())) {
            store.setRegionId(v.getRegionCode());
            store.setRegionName(v.getRegionCode()); // 使用 regionCode 作为 regionName
            if (StringUtils.isNotEmpty(v.getRegionManagerEmail()) && v.getRegionManagerEmail().equals(weComEmployeeInfoResponse.getEmail())) {
                headerInfo.setCaPosition(RoleTypeConstants.AREA_MANAGER);
                //确保currentStoreId与 区域角色对应的ID 一致，以保证 regionId的正确性，以及前端显示 regionName的正确性
                //因为 regionId和regionName是根据 currentStoreId
                headerInfo.setCurrentStoreId(v.getId());
            }
        }
    }


    @Override
    public WecomSessionInfoDTO getLoginSessionUserInfo(String userId) {
        if (StringUtils.isEmpty(userId)) {
            log.error("getWecomUserInfo: userId is null or empty");
            return null;
        }
        String key = REDIS_CACHE_WECOM_USER_INFO.concat(userId);
        String value = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(value)) {
            log.error("get WeCom LoginUserInfo is null :key:{}", key);
            return null;
        }
        redisTemplate.expire(key, Duration.ofDays(1));
        return JsonUtil.convertValue(value, WecomSessionInfoDTO.class);
    }

    @Override
    public JSSDKConfigVo generateJSSDKConfig(String url) {
        // 从配置中心获取企业微信应用配置信息
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        String agentId = configContentVO.getBrand().getAgentId();
        // 构建JSAPI Ticket的缓存key
        String cacheKey = REDIS_CACHE_JS_TICKET_CACHE.concat(getTenantId()).concat(":").concat(agentId);
        // 尝试从缓存中获取JSAPI Ticket
        String cacheDataStr = redisTemplate.opsForValue().get(cacheKey);
        JSAPITicket apiTicketResult;
        if (StringUtils.isNotEmpty(cacheDataStr)) {
            // 如果缓存中存在，则直接使用缓存的Ticket
            apiTicketResult = JsonUtil.convertValue(cacheDataStr, JSAPITicket.class);
        } else {
            // 如果缓存中不存在，则重新获取Ticket并缓存
            // 设置缓存过期时间为Ticket有效期减去30秒，避免临界点问题
            apiTicketResult = weComAdapterClient.getJSAPITicket(agentId);
            redisTemplate.opsForValue().set(cacheKey, JsonUtil.convertToString(apiTicketResult), apiTicketResult.getExpiresIn() - EXPIRES_CLOSE, TimeUnit.SECONDS);
        }
        return getJssdkConfigVo(url, apiTicketResult);
    }

    @Override
    public JSSDKConfigVo generateWechatJSSDKConfig(String url) {
        // 从配置中心获取企业微信应用配置信息
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        String agentId = configContentVO.getBrand().getAgentId();
        // 构建JSAPI Ticket的缓存key
        String cacheKey = REDIS_CACHE_WECHAT_JS_TICKET_CACHE.concat(getTenantId()).concat(":").concat(agentId);
        // 尝试从缓存中获取JSAPI Ticket
        String cacheDataStr = redisTemplate.opsForValue().get(cacheKey);
        JSAPITicket apiTicketResult;
        if (StringUtils.isNotEmpty(cacheDataStr)) {
            // 如果缓存中存在，则直接使用缓存的Ticket
            apiTicketResult = JsonUtil.convertValue(cacheDataStr, JSAPITicket.class);
        } else {
            // 如果缓存中不存在，则重新获取Ticket并缓存
            // 设置缓存过期时间为Ticket有效期减去30秒，避免临界点问题
            apiTicketResult = weComAdapterClient.getWechatJSAPITicket(agentId);
            log.info("getWechatJSAPITicket info:{} ", apiTicketResult);
            redisTemplate.opsForValue().set(cacheKey, JsonUtil.convertToString(apiTicketResult), apiTicketResult.getExpiresIn() - EXPIRES_CLOSE, TimeUnit.SECONDS);
        }
        // 创建JSSDK配置对象
        return getJssdkConfigVo(url, apiTicketResult);
    }
    @Override
    public WecomSessionInfoDTO switchUserInfo(String storeId, String userRole) {
        return  switchUserInfo(SessionUtils.getWecomSession(),storeId, userRole);
    }

    public WecomSessionInfoDTO switchUserInfo(WecomSessionInfoDTO session ,String storeId, String userRole) {
        log.info("switchUserInfo: storeId:{}, userRole:{}", storeId, userRole);
        if (StringUtils.isNotEmpty(storeId) && StringUtils.isNotEmpty(userRole)) {
            if(session.getStores().stream().anyMatch(v -> v.getStoreId().equals(storeId))){
                session.setCurrentStoreId(storeId);
            }else{
                log.error("switchUserInfo 切换店铺失败 未找到店铺信息: storeId:{} userRole:{} ", storeId, userRole);
            }
        }
        if (StringUtils.isNotEmpty(userRole)) {
            boolean thisStoreManager = Boolean.TRUE.equals(session.getCurrentStore().getIsManager()) ;
            if (userRole.equals(RoleTypeConstants.STORE_MANAGER)&&!thisStoreManager) {
                log.error("switchUserInfo  切换店长失败，非店铺管理员，降级为SALES , storeId:{} userRole:{} ", storeId, userRole);
                session.setCaPosition(RoleTypeConstants.SALES);
            }else{
                session.setCaPosition(userRole);
            }
        }
        String  data=JsonUtil.convertToString(session);
        //覆写redis
        redisTemplate.opsForValue()
                .set(REDIS_CACHE_WECOM_USER_INFO.concat(session.getUserId()),data , Duration.ofDays(1));
        log.info("switchUserInfo: return session:{}", data);
        return session;
    }

    private static JSSDKConfigVo getJssdkConfigVo(String url, JSAPITicket apiTicketResult) {
        // 创建JSSDK配置对象
        JSSDKConfigVo configVo = new JSSDKConfigVo();
        // 生成时间戳
        Long tstamp = System.currentTimeMillis() / 1000;
        String timestamp = String.valueOf(tstamp);
        // 生成随机字符串
        String nonceStr = SignUtil.getNonceStr();
        // 使用Ticket、时间戳、URL和随机字符串生成签名
        String sign = SignUtil.sign(apiTicketResult.getTicket(), timestamp, url, nonceStr);
        // 设置配置参数
        configVo.setNonceStr(nonceStr);
        configVo.setTimestamp(timestamp);
        configVo.setTstamp(tstamp);
        configVo.setSignature(sign);
        return configVo;
    }

    private String getTenantId() {
        return GenericRequestContextHolder.getTenantContext()
                .map(TenantContext::getTenantId)
                .orElseThrow(() -> {
                    log.error("getTenantId failed!!!");
                    return new ServiceException(RemotesalesErrorCode.SYSTEM_ERROR);
                });
    }

    private List<LocationVO> getAllStores() {
        List<LocationVO> allStores = new ArrayList<>();
        int pageNum = 1;
        int pageSize = 200;
        
        while (true) {
            LocationQueryDTO queryDTO = new LocationQueryDTO();
            queryDTO.setType(LocationType.STORE);
            queryDTO.setStatus(LocationStatus.ENABLED);
            queryDTO.setPageSize(pageSize);
            queryDTO.setPageNum(pageNum);
            
            R<PageResult<LocationVO>> response = locationFeignClient.page(queryDTO);
            if (response != null && response.getData() != null) {
                List<LocationVO> currentPageData = response.getData().getData();
                if (currentPageData.isEmpty()) {
                    break; // No more data
                }
                allStores.addAll(currentPageData);
                
                // Check if this is the last page
                if (currentPageData.size() < pageSize) {
                    break; // This was the last page
                }
            } else {
                break; // Error or no data
            }
            pageNum++;
        }
        
        return allStores;
    }


    @Override
    public List<WecomSessionInfoDTO.Store> rmStorePerformanceList() {
        WecomSessionInfoDTO session= SessionUtils.getWecomSession();
        ConfigContentVO configContentVO = configCenterClient.getBrandAllConfigContent();
        WeComUserInfo userInfo = weComAdapterClient.getUserInfo(configContentVO.getBrand().getAgentId(), session.getUserId());
        log.info("[rmStorePerformanceList] userInfo:{}", userInfo);
        if (userInfo == null || StringUtils.isEmpty(userInfo.getUserid())) {
            log.error("未获取到访问用户信息");
            throw new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
        }
        WeComUserDetail weComEmployeeInfoResponse = weComAdapterClient.getUserDetail(null, session.getUserId());
        if (Objects.isNull(weComEmployeeInfoResponse)) {
            log.error("未获取到访问用户敏感信息 userId:{}", session.getUserId());
            throw new ServiceException(RemotesalesErrorCode.UNAUTHORIZED);
        }
        return (getAllStores().stream().map(v -> {
            List<String> wecomDepartmentIds = Arrays.asList(v.getWecomDeptId().split(","));
            // 使用 LocationVO 中的区域信息进行过滤
            if(StringUtils.isEmpty(v.getRegionCode()) || !v.getRegionCode().equals(session.getRegionId())){
                return null;
            }
            WecomSessionInfoDTO.Store store = new WecomSessionInfoDTO.Store();
            store.setWecomDeptIds(wecomDepartmentIds.stream().mapToInt(Integer::parseInt).toArray());
            store.setStoreId(v.getId());
            store.setStoreCode(v.getCode());
            store.setStoreName(v.getName());
            store.setMobile(v.getMobile());
            store.setTelephone(v.getTelephone());
            if (StringUtils.isNotEmpty(v.getCountryCode())) {
                store.setCountry(CONTRY_NAME_MAP.get(v.getCountryCode()));
            }
            store.setProvince(v.getProvince());
            store.setCity(v.getCity());
            store.setDistrict(v.getDistrict());
            store.setAddress(v.getAddress());
            store.setZipcode(v.getPostalCode());

            store.setIsManager(StringUtils.isNotEmpty(v.getLocationManagerEmail()) && v.getLocationManagerEmail().equals(weComEmployeeInfoResponse.getEmail()));
            store.setManagerEmail(v.getLocationManagerEmail());
            store.setRegionId(v.getRegionCode());
            store.setRegionName(v.getRegionCode());

            setStoreCaInfo(configContentVO,store, store.getWecomDeptIds());
            return store;
        }).filter(Objects::nonNull).toList());
    }

    private void setStoreCaInfo(ConfigContentVO configContentVO  ,WecomSessionInfoDTO.Store store, int[] intersection) {

        if (StringUtils.isNotEmpty(store.getManagerEmail())) {
            // 获取Manager Wwid
            WeComUserIdResponse userIdByEmail = weComAdapterClient.getUserIdByEmail(configContentVO.getBrand().getAgentId(), store.getManagerEmail());
            if (StringUtils.isNotBlank(userIdByEmail.getUserid())) {
                store.setManagerWwid(userIdByEmail.getUserid());
                Arrays.stream(intersection).forEach(deptId ->{
                    WeComDepartmentUserListResponse departmentUser = weComAdapterClient.getDepartmentUser(configContentVO.getBrand().getAgentId(), deptId+ "");
                    List<WeComDepartmentUserListResponse.DepartmentUser> userList = departmentUser.getUserList();
                    if (CollectionUtils.isNotEmpty(userList)) {
                        String managerName = userList.stream()
                                .filter(item -> item.getUserid().equals(store.getManagerWwid()))
                                .map(WeComDepartmentUserListResponse.DepartmentUser::getName)
                                .findFirst()
                                .orElse(UNKNOWN);
                        store.setManagerName(managerName);
                        store.setCaNumber(userList.size());
                    } else {
                        store.setManagerName(UNKNOWN);
                        store.setCaNumber(0);
                    }
                });

            } else {
                log.error("[Get Store Manager] storeId: {}, managerEmail: {}", store.getStoreId(), store.getManagerEmail());
            }
        }
    }

}
