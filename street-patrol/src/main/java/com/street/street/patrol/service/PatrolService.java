package com.street.street.patrol.service;

import com.street.street.common.dto.LocationDTO;
import com.street.street.common.entity.PatrolTrajectory;
import java.util.List;

public interface PatrolService {
    void receiveLocation(LocationDTO locationDTO);
    List<PatrolTrajectory> getTodayTrajectory(Long userId);
}
