CREATE DATABASE IF NOT EXISTS `street_patrol` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `street_patrol`;

-- ----------------------------
-- Table structure for patrol_record
-- ----------------------------
DROP TABLE IF EXISTS `patrol_record`;
CREATE TABLE `patrol_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '河长ID',
  `river_id` bigint NOT NULL COMMENT '河流ID',
  `start_time` datetime NOT NULL COMMENT '巡河开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '巡河结束时间',
  `total_distance` decimal(10,2) DEFAULT '0.00' COMMENT '总里程(米)',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-进行中, 2-已结束',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='巡河记录主表';

-- ----------------------------
-- Table structure for patrol_trajectory
-- ----------------------------
DROP TABLE IF EXISTS `patrol_trajectory`;
CREATE TABLE `patrol_trajectory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_id` bigint NOT NULL COMMENT '关联巡河记录ID',
  `user_id` bigint NOT NULL COMMENT '河长ID',
  `longitude` decimal(10,7) NOT NULL COMMENT '经度',
  `latitude` decimal(10,7) NOT NULL COMMENT '纬度',
  `speed` decimal(5,2) DEFAULT '0.00' COMMENT '瞬时速度(m/s)',
  `collect_time` datetime NOT NULL COMMENT '硬件采集时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_user_time` (`user_id`,`collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='历史轨迹坐标表';
