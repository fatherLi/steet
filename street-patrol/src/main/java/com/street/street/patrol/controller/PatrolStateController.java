package com.street.street.patrol.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 巡河状态控制器 (管理开始/结束巡河状态，驱动主动拉取)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/patrol/state")
@RequiredArgsConstructor
public class PatrolStateController {

    private final StringRedisTemplate redisTemplate;

    // Redis 中存放活跃巡河用户的 Set Key
    private static final String ACTIVE_USERS_KEY = "patrol:active_users";

    /**
     * 开始巡河
     * 移动端调用后，系统会记录状态并将其加入活跃列表，触发每分钟的数据拉取
     */
    /**
     * 开始巡河接口 (移动端APP/小程序调用)
     * 业务逻辑说明：
     * 当移动端调用此接口时，代表河长开始工作。
     * 系统除了记录基本信息外，最关键的是会向 Redis 中写入该河长正在巡河的标记。
     * 这个标记就是 XXL-JOB 定时任务判断是否要拉取他位置的“开关”。
     *
     * @param userId 河长的唯一ID
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startPatrol(@RequestParam Long userId) {
        log.info("User {} started patrol.", userId);
        
        // 1. 业务逻辑：可以在数据库 patrol_record 插入一条 status=1(进行中) 的记录
        // (略，重点演示动态调度)

        // 2. 将用户加入 Redis 活跃集合
        redisTemplate.opsForSet().add(ACTIVE_USERS_KEY, String.valueOf(userId));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "Patrol started. Background pulling activated.");
        return ResponseEntity.ok(result);
    }

    /**
     * 结束巡河
     * 移动端调用后，更新业务状态，并从活跃列表中移除，停止拉取
     */
    /**
     * 结束巡河接口
     * 业务逻辑说明：
     * 河长结束工作时调用。除了结算本次巡河里程外，
     * 必须将他从 Redis 活跃集合中剔除。这样下一分钟 XXL-JOB 触发时，
     * 就不会再去海康威视平台拉取他执法记录仪的坐标了，节省服务器资源。
     *
     * @param userId 河长的唯一ID
     */
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endPatrol(@RequestParam Long userId) {
        log.info("User {} ended patrol.", userId);

        // 1. 业务逻辑：更新 patrol_record 的 status=2(已结束)，计算总里程等
        // (略)

        // 2. 将用户从 Redis 活跃集合移除
        redisTemplate.opsForSet().remove(ACTIVE_USERS_KEY, String.valueOf(userId));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "Patrol ended. Background pulling deactivated.");
        return ResponseEntity.ok(result);
    }
}
