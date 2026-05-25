package com.street.street.iot.service;

import com.street.street.iot.config.HikvisionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 设备-用户映射服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceUserMappingService {

    private final HikvisionProperties hikvisionProperties;

    /**
     * 根据设备编号获取绑定的河长ID
     * 首先从配置中心 (Nacos / application.yml) 的映射表中查找
     * @param deviceCode 设备唯一标识
     * @return 绑定的河长ID，如果未绑定则返回null
     */
    public Long getUserIdByDeviceCode(String deviceCode) {
        Map<String, Long> mapping = hikvisionProperties.getDeviceMapping();
        if (mapping != null && mapping.containsKey(deviceCode)) {
            return mapping.get(deviceCode);
        }
        log.warn("Device code [{}] not found in configuration mapping.", deviceCode);
        return null;
    }
}
