package com.street.street.patrol.controller;

import com.street.street.common.dto.LocationDTO;
import com.street.street.patrol.service.PatrolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal/patrol")
@RequiredArgsConstructor
public class PatrolInternalController {

    private final PatrolService patrolService;

    @PostMapping("/location")
    public Map<String, Object> receiveLocation(@RequestBody LocationDTO locationDTO) {
        patrolService.receiveLocation(locationDTO);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");
        return result;
    }
}
