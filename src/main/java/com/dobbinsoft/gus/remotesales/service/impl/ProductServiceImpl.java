package com.dobbinsoft.gus.remotesales.service.impl;

import com.dobbinsoft.gus.remotesales.client.product.ProductClient;
import com.dobbinsoft.gus.remotesales.data.vo.product.ProductVo;
import com.dobbinsoft.gus.remotesales.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现类
 *
 * @author yanziyu
 * @date 2025/5/15
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductClient productClient;

    @Override
    public List<String> getSearchSuggestion(String sku) {
        return productClient.getSearchSuggestion(sku);
    }

    @Override
    public ProductVo getItemDetail(String sku , String storeCode) {
        return productClient.getItemDetail(sku, storeCode);
    }
} 