package com.street.street.iot.dto;

import lombok.Data;
import java.util.List;

@Data
public class HikGpsEventDTO {
    private String method;
    private Params params;

    @Data
    public static class Params {
        private String ability;
        private List<HikEvent> events;
    }

    @Data
    public static class HikEvent {
        private String eventId;
        private Integer eventType;
        private String srcIndex;
        private String happenTime;
        private HikGpsData data;
    }

    @Data
    public static class HikGpsData {
        private Double longitude;
        private Double latitude;
        private Double speed;
        private Double direction;
        private Integer satelliteNum;
    }
}
