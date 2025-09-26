package com.dobbinsoft.gus.remotesales.client.product;

import com.dobbinsoft.gus.common.model.enums.Language;
import com.dobbinsoft.gus.common.utils.context.GenericRequestContextHolder;
import com.dobbinsoft.gus.remotesales.client.gus.product.ProductItemFeignClient;
import com.dobbinsoft.gus.remotesales.client.gus.product.ProductStockFeignClient;
import com.dobbinsoft.gus.remotesales.client.gus.product.model.*;
import com.dobbinsoft.gus.remotesales.data.vo.product.ProductVo;
import com.dobbinsoft.gus.remotesales.exception.RemotesalesErrorCode;
import com.dobbinsoft.gus.web.exception.ServiceException;
import com.dobbinsoft.gus.web.vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductClientGusImpl implements ProductClient {

    @Autowired
    private ProductItemFeignClient productItemFeignClient;

    @Autowired
    private ProductStockFeignClient productStockFeignClient;

    @Override
    public List<String> getSearchSuggestion(String sku) {
        return List.of();
    }

    @Override
    public ProductVo getItemDetail(String sku, String locationCode) {
        R<ItemDetailVO> r = productItemFeignClient.getBySku(sku);
        if (!RemotesalesErrorCode.SUCCESS.getCode().equals(r.getCode())) {
            throw new ServiceException(RemotesalesErrorCode.PRODUCT_NOT_FOUND);
        }
        ItemDetailVO itemDetailVO = r.getData();
        ProductVo productVo = new ProductVo();
        productVo.setSku(sku);
        productVo.setSmc(itemDetailVO.getSmc());
        List<ItemVO.ItemDetailInfoVO> details = itemDetailVO.getDetails();
        Language language = GenericRequestContextHolder.getLanguageContext().get().getLanguage();
        ItemVO.ItemDetailInfoVO itemDetailInfoVO = details.stream()
                .filter(info -> language.name().equals(info.getLanguage()))
                .findFirst()
                .orElse(details.getFirst());
        productVo.setProductName(itemDetailInfoVO.getName());
        productVo.setDescription(itemDetailInfoVO.getDescription());
        productVo.setPic(itemDetailVO.getImages().getFirst());

        // SKU
        Optional<ItemVO.ItemSkuVO> skuOptional = itemDetailVO.getSkus().stream().filter(item -> item.getSku().equals(sku)).findFirst();
        ItemVO.ItemSkuVO itemSkuVO = skuOptional.orElseThrow(() -> new ServiceException(RemotesalesErrorCode.PRODUCT_NOT_FOUND));
        productVo.setSize(itemSkuVO.getSpecificationValues().stream().map(ItemSpecificationValueVO::getName).collect(Collectors.joining(",")));

        // Stock & Price
        R<ItemStockVO> itemStockVOR = productStockFeignClient.itemStock(itemDetailVO.getSmc());
        productVo.setStock(0);
        productVo.setPrice(BigDecimal.ZERO);
        if (RemotesalesErrorCode.SUCCESS.getCode().equals(itemStockVOR.getCode())) {
            ItemStockVO itemStockVO = itemStockVOR.getData();
            Optional<ListStockVO> stockOptional = itemStockVO.getStocks()
                    .stream()
                    .filter(s -> s.getLocationCode().equals(locationCode))
                    .findFirst();

            stockOptional.ifPresent(stock -> {
                productVo.setStock(stock.getQuantity().intValue());
                productVo.setPrice(stock.getPrice());
            });
        }

        return productVo;

    }
}
