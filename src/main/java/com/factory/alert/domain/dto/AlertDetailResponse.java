package com.factory.alert.domain.dto;

import com.factory.alert.domain.entity.Alert;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlertDetailResponse {

    private Long alertId;
    private Long logId;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdTime;
    private String severity;
    private Integer occurrenceCount;
    private LocalDateTime lastDetectedAt;

    public static AlertDetailResponse from(Alert alert) {
        return AlertDetailResponse.builder()
                .alertId(alert.getAlertId())
                .logId(alert.getLogId())
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