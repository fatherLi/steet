package com.street.street.patrol.service.impl;

import com.alibaba.fastjson2.JSON;
import com.street.street.common.dto.LocationDTO;
import com.street.street.common.entity.PatrolTrajectory;
import com.street.street.common.util.CoordinateUtil;
import com.street.street.patrol.mapper.PatrolTrajectoryMapper;
import com.street.street.patrol.service.PatrolService;
import com.street.street.patrol.websocket.PatrolWebSocketEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatrolServiceImpl implements PatrolService {

    private final StringRedisTemplate stringRedisTemplate;
    private final PatrolTrajectoryMapper patrolTrajectoryMapper;
    private final PatrolWebSocketEndpoint patrolWebSocketEndpoint;

    private static final String TRAJECTORY_KEY_PREFIX = "patrol:trajectory:";

    @Override
    public void receiveLocation(LocationDTO locationDTO) {
        log.info("Patrol Service - Received location: {}", locationDTO);

        Long userId = locationDTO.getUserId();
        if (userId == null || locationDTO.getLongitude() == null || locationDTO.getLatitude() == null) {
            return;
        }

        double[] gcj02 = CoordinateUtil.wgs84ToGcj02(
                locationDTO.getLongitude().doubleValue(),
                locationDTO.getLatitude().doubleValue()
        );

        long timestamp = locationDTO.getTimestamp() != null ? locationDTO.getTimestamp() : System.currentTimeMillis();
        LocalDateTime collectTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

        PatrolTrajectory trajectory = new PatrolTrajectory();
        trajectory.setUserId(userId);
        trajectory.setRecordId(0L); 
        trajectory.setLongitude(new java.math.BigDecimal(String.valueOf(gcj02[0])));
        trajectory.setLatitude(new java.math.BigDecimal(String.valueOf(gcj02[1])));
        trajectory.setSpeed(locationDTO.getSpeed());
        trajectory.setCollectTime(collectTime);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = TRAJECTORY_KEY_PREFIX + userId + ":" + dateStr;
        
        String jsonStr = JSON.toJSONString(trajectory);
        stringRedisTemplate.opsForZSet().add(redisKey, jsonStr, timestamp);
        stringRedisTemplate.expire(redisKey, 48, TimeUnit.HOURS);

        try {
            patrolTrajectoryMapper.insert(trajectory);
        } catch (Exception e) {
            log.error("Failed to insert trajectory to DB", e);
        }

        patrolWebSocketEndpoint.pushLocationToUser(userId, jsonStr);
    }

    @Override
    public List<PatrolTrajectory> getTodayTrajectory(Long userId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = TRAJECTORY_KEY_PREFIX + userId + ":" + dateStr;

        Set<String> jsonSet = stringRedisTemplate.opsForZSet().range(redisKey, 0, -1);
        List<PatrolTrajectory> list = new ArrayList<>();
        if (jsonSet != null) {
            for (String str : jsonSet) {
                list.add(JSON.parseObject(str, PatrolTrajectory.class));
            }
        }
        return list;
    }
}
