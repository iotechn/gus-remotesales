package com.dobbinsoft.gus.remotesales.client.gus.logistics;

import com.dobbinsoft.gus.remotesales.client.gus.logistics.model.ExpressOrderCreateDTO;
import com.dobbinsoft.gus.remotesales.client.gus.logistics.model.ExpressOrderVO;
import com.dobbinsoft.gus.remotesales.client.gus.logistics.model.LpCode;
import com.dobbinsoft.gus.remotesales.configuration.OpenFeignConfig;
import com.dobbinsoft.gus.web.vo.R;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "logistics-express",
        url = "http://gus-logistics",
        path = "/api/express",
        configuration = OpenFeignConfig.class)
public interface ExpressFeignClient {

    @PostMapping
    R<ExpressOrderVO> create(
            @Valid @RequestBody ExpressOrderCreateDTO createDTO);

    @GetMapping
    R<ExpressOrderVO> get(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) LpCode lpCode,
            @RequestParam(required = false) String transNo,
            @RequestParam(required = false) String mobile);

}
