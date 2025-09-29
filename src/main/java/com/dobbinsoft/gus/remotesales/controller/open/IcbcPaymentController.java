//package com.dobbinsoft.gus.remotesales.controller.openApi;
//
//import com.dobbinsoft.gus.web.vo.R;
//import com.icbc.api.IcbcApiException;
//import com.dobbinsoft.gus.lib.common.PlatformHeaders;
//import com.dobbinsoft.gus.lib.common.context.GenericRequestContextHolder;
//import com.dobbinsoft.gus.lib.common.context.bo.IdentityContext;
//import com.dobbinsoft.gus.lib.common.context.bo.RequestProperty;
//import com.dobbinsoft.gus.lib.common.context.bo.TenantContext;
//import com.dobbinsoft.gus.lib.common.context.bo.TraceContext;
//import com.dobbinsoft.gus.remotesales.client.icbc.vo.Notify;
//import com.dobbinsoft.gus.remotesales.data.vo.icbc.IcbcResponseVo;
//import com.dobbinsoft.gus.remotesales.service.ICBCCallBackService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@Slf4j
//@RestController
//@RequestMapping("/icbc-payment")
//@Tag(name = "ICBC回调相关接口")
//public class IcbcPaymentController {
//
//    @Autowired
//    private ICBCCallBackService icbcCallBackService;
//
//    @PostMapping("/callback/{tenantId}")
//    @Operation(summary = "ICBC回调接口")
//    public R<IcbcResponseVo> callback(@PathVariable String tenantId, @RequestBody Notify notify) throws IcbcApiException {
//        log.info("[ICBC Callback] invoked tenantId:{}", tenantId);
//        // 手动设置租户信息
//        RequestProperty requestProperty = RequestProperty.builder()
//                .tenantContext(new TenantContext())
//                .traceContext(new TraceContext())
//                .identityContext(new IdentityContext())
//                .build();
//        requestProperty.setProperty(PlatformHeaders.TENANT_ID.name(), tenantId);
//        requestProperty.initContext();
//        GenericRequestContextHolder.setRequestProperty(requestProperty);
//        return R.success(icbcCallBackService.notify(notify));
//    }
// TODO payment
//
//}
