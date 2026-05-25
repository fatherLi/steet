package com.street.street.patrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("patrol_record")
public class PatrolRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long riverId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private BigDecimal totalDistance;
    
    private Integer status;
}
