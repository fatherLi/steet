package com.street.street.iot.controller;

import com.street.street.common.dto.LocationDTO;
import com.street.street.iot.dto.HikGpsEventDTO;
import com.street.street.iot.service.DeviceUserMappingService;
import com.street.street.iot.feign.PatrolFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/hikvision")
@RequiredArgsConstructor
public class HikvisionReceiverController {

    private final PatrolFeignClient patrolFeignClient;
    private final DeviceUserMappingService mappingService;

    @PostMapping("/event/receive")
    public ResponseEntity<Map<String, Object>> receiveHikvisionEvent(@RequestBody HikGpsEventDTO hikEvent) {
        log.info("Received Hikvision Webhook event: {}", hikEvent.getMethod());

        if (hikEvent.getParams() != null && hikEvent.getParams().getEvents() != null) {
            for (HikGpsEventDTO.HikEvent event : hikEvent.getParams().getEvents()) {
                String deviceCode = event.getSrcIndex();
                Long userId = mappingService.getUserIdByDeviceCode(deviceCode);
                if (userId == null) {
                    log.warn("Unbound Hikvision device: {}", deviceCode);
                    continue;
                }

                HikGpsEventDTO.HikGpsData data = event.getData();
                if (data != null && data.getLongitude() != null && data.getLatitude() != null) {
                    LocationDTO locationDTO = new LocationDTO();
                    locationDTO.setUserId(userId);
                    locationDTO.setLongitude(new BigDecimal(data.getLongitude().toString()));
                    locationDTO.setLatitude(new BigDecimal(data.getLatitude().toString()));
                    locationDTO.setSpeed(data.getSpeed() != null ? new BigDecimal(data.getSpeed().toString()) : BigDecimal.ZERO);
                    try {
                        long timestamp = Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(event.getHappenTime())).toEpochMilli();
                        locationDTO.setTimestamp(timestamp);
                    } catch (Exception e) {
                        locationDTO.setTimestamp(System.currentTimeMillis());
                    }

                    // 调用内部核心业务服务
                    patrolFeignClient.receiveLocation(locationDTO);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return ResponseEntity.ok(result);
    }
}
