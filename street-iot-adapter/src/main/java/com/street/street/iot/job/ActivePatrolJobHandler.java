package com.street.street.iot.job;

import com.street.street.common.dto.LocationDTO;
import com.street.street.iot.config.HikvisionProperties;
import com.street.street.iot.feign.PatrolFeignClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivePatrolJobHandler {

    private final StringRedisTemplate redisTemplate;
    private final HikvisionProperties hikvisionProperties;
    private final PatrolFeignClient patrolFeignClient;

    // Redis 中存放活跃巡河用户的 Set Key
    private static final String ACTIVE_USERS_KEY = "patrol:active_users";

    /**
     * XXL-JOB 定时任务处理器
     * 建议在 XXL-JOB Admin 中配置为每 1 分钟执行一次 (Cron: 0 * * * * ?)
     */
    /**
     * XXL-JOB 定时任务核心处理器 (主动轮询引擎)
     * 
     * 部署说明：
     * 需要在 XXL-JOB Admin 控制台新建一个任务，JobHandler 填 "patrolGpsPullJob"。
     * Cron 表达式建议配置为 "0 * * * * ?"，即每当秒数为0时触发（也就是每 1 分钟执行一次）。
     * 
     * 业务逻辑：
     * 这个方法是整个“主动拉取”架构的心脏。它每分钟被唤醒一次，然后去检查
     * 当前有哪些人正在巡河，并向海康平台索要这些人的位置信息。
     */
    @XxlJob("patrolGpsPullJob")
    public void pullGpsJob() {
        XxlJobHelper.log("开始执行海康设备定位主动拉取任务...");

        // 1. 获取当前所有正在巡河的用户
        Set<String> activeUsers = redisTemplate.opsForSet().members(ACTIVE_USERS_KEY);
        if (activeUsers == null || activeUsers.isEmpty()) {
            XxlJobHelper.log("当前无人在巡河，跳过拉取。");
            return;
        }

        // 2. 获取配置中的设备映射表
        Map<String, Long> deviceMapping = hikvisionProperties.getDeviceMapping();
        if (deviceMapping == null || deviceMapping.isEmpty()) {
            XxlJobHelper.log("设备映射表为空，无法拉取！");
            return;
        }

        int successCount = 0;
        
        // 3. 遍历活跃用户，通过他们的 userId 反查绑定的 deviceCode
        // 实际企业开发中，反查过程一般从数据库查询，这里为了演示，双向遍历映射表
        for (String userIdStr : activeUsers) {
            Long targetUserId = Long.valueOf(userIdStr);
            String targetDeviceCode = null;
            
            for (Map.Entry<String, Long> entry : deviceMapping.entrySet()) {
                if (entry.getValue().equals(targetUserId)) {
                    targetDeviceCode = entry.getKey();
                    break;
                }
            }

            // 匹配到了设备，开始正式向海康发起请求
            if (targetDeviceCode != null) {
                // 4. 调用海康 OpenAPI 获取该设备的当前定位
                // 注意：在实际开发中，这里 pullFromHikvisionApi 方法内部需要构造符合海康要求的 HTTP 请求，
                // 通常需要带上 AK、SK 算出来的签名(Signature)放到 Header 中。
                LocationDTO location = pullFromHikvisionApi(targetUserId, targetDeviceCode);
                
                // 如果成功拿到了坐标数据
                if (location != null) {
                    // 5. 通过 OpenFeign 跨微服务调用 `street-patrol` 的接收接口
                    // 将脏活累活（解析外部协议）留在当前模块，把干净的标准数据（LocationDTO）传给核心模块
                    patrolFeignClient.receiveLocation(location);
                    successCount++;
                }
            }
        }
        
        XxlJobHelper.log("定位拉取任务执行完成，共成功拉取 " + successCount + " 个设备。");
    }

    /**
     * 手动触发拉取某人的定位
     */
    public LocationDTO manualPull(Long userId, String deviceCode) {
        LocationDTO location = pullFromHikvisionApi(userId, deviceCode);
        if (location != null) {
            patrolFeignClient.receiveLocation(location);
        }
        return location;
    }

    /**
     * 模拟调用海康威视 Artemis 开放平台的 API
     * 实际中这里应该构造带 AK/SK 签名的 HTTP GET/POST 请求
     */
    private LocationDTO pullFromHikvisionApi(Long userId, String deviceCode) {
        log.info("Mock HTTP Request -> GET {}/api/v1/mobile/status/query?deviceCode={}", hikvisionProperties.getHost(), deviceCode);
        
        // 模拟网络延迟
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        // 模拟返回随机经纬度 (以天安门为中心随机偏移)
        double baseLng = 116.397428;
        double baseLat = 39.909230;
        double offsetLng = (Math.random() - 0.5) * 0.01;
        double offsetLat = (Math.random() - 0.5) * 0.01;

        LocationDTO dto = new LocationDTO();
        dto.setUserId(userId);
        dto.setLongitude(new BigDecimal(String.valueOf(baseLng + offsetLng)));
        dto.setLatitude(new BigDecimal(String.valueOf(baseLat + offsetLat)));
        dto.setSpeed(new BigDecimal("1.2"));
        dto.setTimestamp(System.currentTimeMillis());

        return dto;
    }
}
