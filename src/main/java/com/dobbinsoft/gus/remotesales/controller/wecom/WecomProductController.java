package com.dobbinsoft.gus.remotesales.controller.wecom;

import com.dobbinsoft.gus.remotesales.data.vo.product.ProductVo;
import com.dobbinsoft.gus.remotesales.data.dto.session.WecomSessionInfoDTO;
import com.dobbinsoft.gus.remotesales.service.ProductService;
import com.dobbinsoft.gus.remotesales.utils.SessionUtils;
import com.dobbinsoft.gus.web.vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * describe:
 *
 * @author yanziyu
 * @date 2025/5/15 15:21
 */
@RestController
@Tag(name = "企业微信商品接口")
@RequestMapping(("/wecom/product"))
public class WecomProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/search-suggestion/{sku}")
    @Operation(summary = "模糊搜索商品")
    public R<List<String>> getItem(@PathVariable String sku) {
        List<String> item = productService.getSearchSuggestion(sku);
        return R.success(item);
    }

    @GetMapping("/detail/{sku}")
    @Operation(summary = "获取SKU详情")
    public R<ProductVo> getItemDetail(@PathVariable String sku) {
        WecomSessionInfoDTO wecomSession = SessionUtils.getWecomSession();
        ProductVo itemDetail = productService.getItemDetail(sku, wecomSession.getStoreCode());
        return R.success(itemDetail);
    }
}
