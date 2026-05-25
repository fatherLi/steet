package com.street.street.iot.controller;

import com.street.street.common.dto.LocationDTO;
import com.street.street.iot.job.ActivePatrolJobHandler;
import com.street.street.iot.service.DeviceUserMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 海康设备交互网关控制器
 * 
 * 作用：
 * 主要负责接收来自前端或管理后台的【手动触发】类请求。
 * 在彻底改为 Active Polling (主动拉取) 架构后，原先被动等待海康 Webhook 的接口已被删除，
 * 彻底杜绝了因内网防火墙导致海康无法回调我们的问题。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hikvision")
@RequiredArgsConstructor
public class HikvisionReceiverController {

    private final ActivePatrolJobHandler activePatrolJobHandler;
    private final DeviceUserMappingService mappingService;

    /**
     * 手动触发拉取特定设备的定位 (例如河长点击刷新)
     * 
     * 业务逻辑：
     * 1. 接收前端传入的设备编码
     * 2. 校验该设备是否已经在系统中绑定了对应的河长
     * 3. 强制触发一次海康 API 调用获取最新坐标并走完后续推流流程
     * 
     * @param deviceCode 硬件编号 (如海康的设备序列号)
     * @return 包含最新位置信息的 JSON 响应
     */
    @PostMapping("/manual-pull")
    public ResponseEntity<Map<String, Object>> manualPull(@RequestParam String deviceCode) {
        Long userId = mappingService.getUserIdByDeviceCode(deviceCode);
        // 安全校验：如果设备未绑定任何用户，则直接拒绝服务
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("msg", "Device not bound");
            return ResponseEntity.badRequest().body(error);
        }

        // 调用 Job Handler 中的核心拉取逻辑
        LocationDTO location = activePatrolJobHandler.manualPull(userId, deviceCode);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", location);
        return ResponseEntity.ok(result);
    }
}
