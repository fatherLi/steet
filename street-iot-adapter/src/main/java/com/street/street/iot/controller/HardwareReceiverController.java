package com.street.street.iot.controller;

import com.street.street.common.dto.LocationDTO;
import com.street.street.iot.feign.PatrolFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patrol/hardware")
@RequiredArgsConstructor
public class HardwareReceiverController {

    private final PatrolFeignClient patrolFeignClient;

    @PostMapping("/location")
    public ResponseEntity<Map<String, Object>> uploadLocation(@RequestBody LocationDTO locationDTO) {
        // 直接转发给 patrol 核心业务微服务
        patrolFeignClient.receiveLocation(locationDTO);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return ResponseEntity.ok(result);
    }
}
