package com.street.street.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class LocationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal speed;
    private Long timestamp;
}
