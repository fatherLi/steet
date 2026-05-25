package com.street.street.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("patrol_trajectory")
public class PatrolTrajectory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long userId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal speed;
    private LocalDateTime collectTime;
}
