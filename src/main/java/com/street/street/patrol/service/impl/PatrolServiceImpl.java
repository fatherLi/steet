package com.street.street.patrol.service.impl;

import com.alibaba.fastjson2.JSON;
import com.street.street.patrol.dto.LocationDTO;
import com.street.street.patrol.entity.PatrolTrajectory;
import com.street.street.patrol.mapper.PatrolTrajectoryMapper;
import com.street.street.patrol.service.PatrolService;
import com.street.street.patrol.util.CoordinateUtil;
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

    // Redis Key 前缀
    private static final String TRAJECTORY_KEY_PREFIX = "patrol:trajectory:";

    @Override
    public void receiveLocation(LocationDTO locationDTO) {
        log.info("Received location: {}", locationDTO);

        Long userId = locationDTO.getUserId();
        if (userId == null || locationDTO.getLongitude() == null || locationDTO.getLatitude() == null) {
            log.warn("Invalid location data: {}", locationDTO);
            return;
        }

        // 1. 坐标系转换 (假设硬件上传的是 WGS84，前端地图高德使用的是 GCJ02)
        double[] gcj02 = CoordinateUtil.wgs84ToGcj02(
                locationDTO.getLongitude().doubleValue(),
                locationDTO.getLatitude().doubleValue()
        );

        long timestamp = locationDTO.getTimestamp() != null ? locationDTO.getTimestamp() : System.currentTimeMillis();
        LocalDateTime collectTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

        // 构造实体，为了后续存入 DB 和 Redis
        PatrolTrajectory trajectory = new PatrolTrajectory();
        trajectory.setUserId(userId);
        // 这里假设只处理单次巡河任务，如果有多任务需要查询当前任务ID。此处设为0作为演示。
        trajectory.setRecordId(0L); 
        trajectory.setLongitude(new java.math.BigDecimal(String.valueOf(gcj02[0])));
        trajectory.setLatitude(new java.math.BigDecimal(String.valueOf(gcj02[1])));
        trajectory.setSpeed(locationDTO.getSpeed());
        trajectory.setCollectTime(collectTime);

        // 2. 存入 Redis 热数据 (使用 ZSET，以时间戳作为 score)
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = TRAJECTORY_KEY_PREFIX + userId + ":" + dateStr;
        
        String jsonStr = JSON.toJSONString(trajectory);
        stringRedisTemplate.opsForZSet().add(redisKey, jsonStr, timestamp);
        // 设置过期时间，比如 48 小时后自动清除 Redis 缓存
        stringRedisTemplate.expire(redisKey, 48, TimeUnit.HOURS);

        // 3. 异步持久化到 MySQL (企业级通常使用 MQ 削峰后再入库，这里为简化直接插入或使用线程池)
        // 注意：高频上报时不建议直接插入，此处仅作演示
        try {
            patrolTrajectoryMapper.insert(trajectory);
        } catch (Exception e) {
            log.error("Failed to insert trajectory to DB", e);
        }

        // 4. WebSocket 实时推送给前端地图
        patrolWebSocketEndpoint.pushLocationToUser(userId, jsonStr);
    }

    @Override
    public List<PatrolTrajectory> getTodayTrajectory(Long userId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = TRAJECTORY_KEY_PREFIX + userId + ":" + dateStr;

        // 从 Redis 取出当天的所有轨迹点
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
