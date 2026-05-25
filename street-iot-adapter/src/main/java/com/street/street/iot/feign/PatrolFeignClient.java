package com.street.street.iot.feign;

import com.street.street.common.dto.LocationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "street-patrol", path = "/api/v1/internal/patrol")
public interface PatrolFeignClient {

    @PostMapping("/location")
    Map<String, Object> receiveLocation(@RequestBody LocationDTO locationDTO);
}
