package com.dobbinsoft.gus.remotesales.service;

import com.dobbinsoft.gus.remotesales.data.vo.product.ProductVo;

import java.util.List;

/**
 * 商品服务接口
 * 提供商品相关的业务逻辑处理
 *
 * @author yanziyu
 * @date 2025/5/15
 */
public interface ProductService {
    
    /**
     * 根据SKU获取商品列表
     * 支持模糊搜索，返回匹配的商品SKU列表
     *
     * @param sku 商品SKU，支持模糊匹配
     * @return 匹配的商品SKU列表
     */
    List<String> getSearchSuggestion(String sku);

    /**
     * 获取商品详细信息
     * 根据SKU、店铺ID和用户ID获取商品的详细信息
     *
     * @param sku 商品SKU
     * @param storeCode 店铺CODE
     * @return 商品详细信息
     */
    ProductVo getItemDetail(String sku , String storeCode);
} 