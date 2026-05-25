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

        // 1. 坐标系纠偏转换 (极为重要)
        // 海康、大华等硬件设备上的 GPS 模块直出的通常是原始国际标准坐标 (WGS84)。
        // 但是咱们前端用的是高德地图，高德使用的是国家测绘局制定的加密坐标系 (GCJ-02，俗称火星坐标系)。
        // 如果不转，直接把 WGS84 的经纬度给高德地图，轨迹在地图上看起来会漂移大概几百米。
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

        // 2. 将数据存入 Redis 作为“热数据缓存”
        // 为什么不用普通 key？因为 ZSET (有序集合) 可以按时间戳(Score)给坐标排序。
        // 这样前端打开地图请求“当天轨迹”时，我们可以飞快地按时间先后把坐标甩给前端画线，性能极高。
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = TRAJECTORY_KEY_PREFIX + userId + ":" + dateStr;
        
        String jsonStr = JSON.toJSONString(trajectory);
        stringRedisTemplate.opsForZSet().add(redisKey, jsonStr, timestamp);
        // 给 Redis 加上 48 小时的过期时间，防止时间久了撑爆服务器内存
        stringRedisTemplate.expire(redisKey, 48, TimeUnit.HOURS);

        // 3. 将数据存入 MySQL 数据库，作为“冷数据”永久保存，供以后查验
        // 企业级优化建议：如果硬件非常多，这里每秒都会被调几千次，直接 insert 会拖垮数据库。
        // 建议以后在这里引入 Kafka，把 trajectory 丢进 MQ 里慢慢 insert。
        try {
            patrolTrajectoryMapper.insert(trajectory);
        } catch (Exception e) {
            log.error("Failed to insert trajectory to DB", e);
        }

        // 4. WebSocket 实时推流
        // 拿到 userId 对应的长链接，把最新的定位直接 push 到河长的浏览器/手机APP里，
        // 前端的标点就会“唰”的一下移动到新位置，实现平滑追踪。
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
