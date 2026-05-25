package com.street.street.patrol.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class LocationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 硬件设备对应的河长ID
     */
    private Long userId;

    /**
     * 经度 (支持 WGS84 或 GCJ02，后端根据配置处理)
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 速度 (m/s)
     */
    private BigDecimal speed;

    /**
     * 时间戳 (秒或毫秒，根据实际情况定，这里假设为毫秒)
     */
    private Long timestamp;
}
