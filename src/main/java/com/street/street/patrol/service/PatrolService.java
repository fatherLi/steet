package com.street.street.patrol.service;

import com.street.street.patrol.dto.LocationDTO;
import com.street.street.patrol.entity.PatrolTrajectory;

import java.util.List;

public interface PatrolService {

    /**
     * 接收硬件设备上传的坐标
     * @param locationDTO 坐标数据
     */
    void receiveLocation(LocationDTO locationDTO);

    /**
     * 获取指定用户当天的巡河轨迹
     * @param userId 河长ID
     * @return 轨迹点列表
     */
    List<PatrolTrajectory> getTodayTrajectory(Long userId);
}
