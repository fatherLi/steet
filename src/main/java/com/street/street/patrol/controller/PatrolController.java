package com.street.street.patrol.controller;

import com.street.street.patrol.dto.LocationDTO;
import com.street.street.patrol.entity.PatrolTrajectory;
import com.street.street.patrol.service.PatrolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patrol")
@RequiredArgsConstructor
public class PatrolController {

    private final PatrolService patrolService;

    /**
     * 模拟硬件通过 HTTP POST 上报 GPS 坐标
     * @param locationDTO
     * @return
     */
    @PostMapping("/hardware/location")
    public ResponseEntity<Map<String, Object>> uploadLocation(@RequestBody LocationDTO locationDTO) {
        patrolService.receiveLocation(locationDTO);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return ResponseEntity.ok(result);
    }

    /**
     * 前端获取河长当日的历史轨迹点列表，用于地图初始化画线
     * @param userId 河长ID
     * @return
     */
    @GetMapping("/trajectory/today")
    public ResponseEntity<Map<String, Object>> getTodayTrajectory(@RequestParam Long userId) {
        List<PatrolTrajectory> list = patrolService.getTodayTrajectory(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }
}
