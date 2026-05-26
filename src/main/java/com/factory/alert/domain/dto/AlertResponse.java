package com.factory.alert.domain.dto;

import com.factory.alert.domain.entity.Alert;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlertResponse {

    private Long alertId;
    private Long logId;
    private String equipmentId;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdTime;
    private String severity;
    private Integer occurrenceCount;
    private LocalDateTime lastDetectedAt;

    public static AlertResponse from(Alert alert) {
        return AlertResponse.builder()
                .alertId(alert.getAlertId())
                .logId(alert.getLogId())
                .equipmentId(alert.getEquipmentId())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .isRead(alert.getIsRead())
                .createdTime(alert.getCreatedTime())
                .severity(alert.getSeverity())
                .occurrenceCount(alert.getOccurrenceCount())
                .lastDetectedAt(alert.getLastDetectedAt())
                .build();
    }
}