package com.street.street.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 海康威视对接参数配置类
 * 企业级开发中，所有的外部对接参数都必须外置化到配置文件或配置中心(Nacos)中
 */
@Data
@Component
@ConfigurationProperties(prefix = "hikvision")
public class HikvisionProperties {

    /**
     * 海康 ISC / 综合安防平台的 AK (App Key)
     */
    private String appKey;

    /**
     * 海康 ISC / 综合安防平台的 SK (App Secret)
     */
    private String appSecret;

    /**
     * 海康平台的主机地址 (如 https://10.x.x.x:443)
     */
    private String host;

    /**
     * 接收推送数据的 Topic 或 Event 类型标识
     */
    private String eventTopic;

    /**
     * 设备编号 (Device Code) 到 内部系统河长 (User ID) 的映射配置
     * 实际生产中往往在关系型数据库维护，这里作为快速启动和轻量级配置演示
     */
    private Map<String, Long> deviceMapping;
}
