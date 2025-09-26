package com.dobbinsoft.gus.remotesales.client.configcenter;

import com.dobbinsoft.gus.common.utils.context.GenericRequestContextHolder;
import com.dobbinsoft.gus.common.utils.context.bo.TenantContext;
import com.dobbinsoft.gus.common.utils.json.JsonUtil;
import com.dobbinsoft.gus.remotesales.client.configcenter.vo.ConfigContentVO;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.remotesales.properties.RemotesaleProperties;
import com.dobbinsoft.gus.remotesales.utils.AESUtil;
import com.dobbinsoft.gus.web.exception.ServiceException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class ConfigCenterClientImpl implements ConfigCenterClient {

    private static final String CONFIG_KEY_PREFIX = "config:tenant:";
    private static final Duration CACHE_EXPIRE_TIME = Duration.ofDays(30); // 配置缓存30天

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private RemotesaleProperties remotesaleProperties;

    @Override
    public void save(ConfigContentVO configContentVO) {
        try {
            String tenantId = getTenantId();
            String cacheKey = buildCacheKey(tenantId);
            
            // 对secret配置进行AES加密
            ConfigContentVO encryptedConfig = encryptSecretConfig(configContentVO);
            
            // 序列化为JSON字符串
            String configJson = JsonUtil.convertToString(encryptedConfig);
            
            // 保存配置到Redis
            stringRedisTemplate.opsForValue().set(cacheKey, configJson, CACHE_EXPIRE_TIME);
            
            log.info("配置保存成功，租户ID: {}, 缓存Key: {}", tenantId, cacheKey);
        } catch (Exception e) {
            log.error("保存配置失败", e);
            throw new ServiceException(RemotesalesErrorCode.SYSTEM_ERROR, "保存配置失败: " + e.getMessage());
        }
    }

    @Override
    public ConfigContentVO getBrandAllConfigContent() {
        try {
            String tenantId = getTenantId();
            String cacheKey = buildCacheKey(tenantId);
            
            // 从Redis读取配置JSON字符串
            String configJson = stringRedisTemplate.opsForValue().get(cacheKey);
            
            ConfigContentVO config;
            if (configJson == null) {
                log.info("配置缓存为空，初始化默认配置，租户ID: {}", tenantId);
                config = createDefaultConfig();
                // 保存默认配置到Redis
                save(config);
            } else {
                // 反序列化JSON
                config = JsonUtil.convertValue(configJson, ConfigContentVO.class);
                // 解密secret配置
                config = decryptSecretConfig(config);
            }
            
            return config;
        } catch (Exception e) {
            log.error("获取配置失败", e);
            // 如果Redis读取失败，返回默认配置
            return createDefaultConfig();
        }
    }


    /**
     * 构建缓存Key，确保租户隔离
     */
    private String buildCacheKey(String tenantId) {
        return CONFIG_KEY_PREFIX + tenantId;
    }

    /**
     * 加密secret配置
     */
    private ConfigContentVO encryptSecretConfig(ConfigContentVO config) {
        if (config == null || config.getSecret() == null) {
            return config;
        }
        
        try {
            ConfigContentVO encryptedConfig = JsonUtil.convertValue(JsonUtil.convertToString(config), ConfigContentVO.class);
            ConfigContentVO.Secret secret = encryptedConfig.getSecret();
            
            if (secret.getWechatAppId() != null) {
                secret.setWechatAppId(AESUtil.encrypt(secret.getWechatAppId(), remotesaleProperties.getAesKey()));
            }
            if (secret.getWechatAppSecret() != null) {
                secret.setWechatAppSecret(AESUtil.encrypt(secret.getWechatAppSecret(), remotesaleProperties.getAesKey()));
            }
            if (secret.getWecomCorpId() != null) {
                secret.setWecomCorpId(AESUtil.encrypt(secret.getWecomCorpId(), remotesaleProperties.getAesKey()));
            }
            if (secret.getWecomCorpSecret() != null) {
                secret.setWecomCorpSecret(AESUtil.encrypt(secret.getWecomCorpSecret(), remotesaleProperties.getAesKey()));
            }
            
            return encryptedConfig;
        } catch (Exception e) {
            log.error("加密secret配置失败", e);
            return config; // 加密失败时返回原配置
        }
    }

    /**
     * 解密secret配置
     */
    private ConfigContentVO decryptSecretConfig(ConfigContentVO config) {
        if (config == null || config.getSecret() == null) {
            return config;
        }
        
        try {
            ConfigContentVO.Secret secret = config.getSecret();
            
            if (secret.getWechatAppId() != null) {
                secret.setWechatAppId(AESUtil.decrypt(secret.getWechatAppId(), remotesaleProperties.getAesKey()));
            }
            if (secret.getWechatAppSecret() != null) {
                secret.setWechatAppSecret(AESUtil.decrypt(secret.getWechatAppSecret(), remotesaleProperties.getAesKey()));
            }
            if (secret.getWecomCorpId() != null) {
                secret.setWecomCorpId(AESUtil.decrypt(secret.getWecomCorpId(), remotesaleProperties.getAesKey()));
            }
            if (secret.getWecomCorpSecret() != null) {
                secret.setWecomCorpSecret(AESUtil.decrypt(secret.getWecomCorpSecret(), remotesaleProperties.getAesKey()));
            }
            
            return config;
        } catch (Exception e) {
            log.error("解密secret配置失败", e);
            return config; // 解密失败时返回原配置
        }
    }

    /**
     * 创建默认配置
     */
    private ConfigContentVO createDefaultConfig() {
        ConfigContentVO config = new ConfigContentVO();
        
        // 初始化品牌配置
        config.setBrand(new ConfigContentVO.Brand());
        
        // 初始化密钥配置
        config.setSecret(new ConfigContentVO.Secret());
        
        // 初始化各种功能配置，默认都关闭
        config.setDeposit(new ConfigContentVO.Deposit(false));
        config.setAutocomplete(new ConfigContentVO.Autocomplete(false));
        config.setStockVisibility(new ConfigContentVO.StockVisibility(false));
        config.setModifyPrice(new ConfigContentVO.ModifyPrice(false));
        config.setMarkdown(new ConfigContentVO.Markdown(false));
        config.setSplitPayment(new ConfigContentVO.SplitPayment(false));
        config.setDeliverySelection(new ConfigContentVO.DeliverySelection(false, ConfigContentVO.DeliverySelectionType.SA));
        config.setRefund(new ConfigContentVO.Refund(false, null, null));
        config.setOrderExpiry(new ConfigContentVO.OrderExpiry(false, 0));
        config.setResetDelivery(new ConfigContentVO.ResetDelivery(false));
        config.setLogisticsAutoConfirm(new ConfigContentVO.LogisticsAutoConfirm(false, 0));
        config.setPickupAutoConfirm(new ConfigContentVO.PickupAutoConfirm(false, 0));
        config.setQrExpiry(new ConfigContentVO.QrExpiry(false, 0));
        config.setFapiao(new ConfigContentVO.Fapiao(false));
        config.setReceiptNo(new ConfigContentVO.ReceiptNo(false));
        config.setExtra(new ConfigContentVO.Extra(false, false, "", "0"));
        
        return config;
    }

    public String getTenantId() {
        return GenericRequestContextHolder.getTenantContext()
                .map(TenantContext::getTenantId)
                .orElseThrow(() -> new ServiceException(RemotesalesErrorCode.SYSTEM_ERROR));
    }
}
