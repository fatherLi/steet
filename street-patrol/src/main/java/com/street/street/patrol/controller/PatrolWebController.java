package com.street.street.patrol.controller;

import com.street.street.common.entity.PatrolTrajectory;
import com.street.street.patrol.service.PatrolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patrol/web")
@RequiredArgsConstructor
public class PatrolWebController {

    private final PatrolService patrolService;

    @GetMapping("/trajectory/today")
    public ResponseEntity<Map<String, Object>> getTodayTrajectory(@RequestParam Long userId) {
        List<PatrolTrajectory> list = patrolService.getTodayTrajectory(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        return ResponseEntity.ok(result);
    }
}
