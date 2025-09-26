package com.dobbinsoft.gus.remotesales.client.gus.location;

import com.dobbinsoft.gus.common.model.vo.PageResult;
import com.dobbinsoft.gus.remotesales.client.gus.location.model.LocationBatchQueryDTO;
import com.dobbinsoft.gus.remotesales.client.gus.location.model.LocationQueryDTO;
import com.dobbinsoft.gus.remotesales.client.gus.location.model.LocationVO;
import com.dobbinsoft.gus.web.vo.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "gus-location", url = "http://gus-location", path = "/api/location")
public interface LocationFeignClient {

    @GetMapping("/{locationId}")
    R<LocationVO> detail(@PathVariable("locationId") String locationId);

    @GetMapping
    R<PageResult<LocationVO>> page(LocationQueryDTO queryDTO);

    @PostMapping("/batch")
    R<List<LocationVO>> batchQuery(@RequestBody LocationBatchQueryDTO batchQueryDTO);

}
