package com.street.street.iot.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeviceUserMappingService {
    private static final Map<String, Long> deviceToUserMap = new HashMap<>();

    static {
        deviceToUserMap.put("HK-DEVICE-001", 1001L);
    }

    public Long getUserIdByDeviceCode(String deviceCode) {
        return deviceToUserMap.get(deviceCode);
    }
}
