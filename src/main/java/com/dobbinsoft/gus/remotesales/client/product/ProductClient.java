package com.dobbinsoft.gus.remotesales.client.product;

import com.dobbinsoft.gus.remotesales.data.vo.product.ProductVo;

import java.util.List;

public interface ProductClient {

    List<String> getSearchSuggestion(String sku);

    ProductVo getItemDetail(String sku , String locationCode);
}
