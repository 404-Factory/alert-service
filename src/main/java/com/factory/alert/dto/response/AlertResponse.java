package com.factory.alert.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertResponse {

    private final Long id;
    private final Long anomalyId;
    private final Long equipmentId;
    private final String title;
    private final String message;
    private final String status;
    private final String severity;
    private final Instant createdAt;
    private final Instant updatedAt;

    @Builder
    public AlertResponse(Long id, Long anomalyId, Long equipmentId, String title, String message,
        String status, String severity, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.anomalyId = anomalyId;
        this.equipmentId = equipmentId;
        this.title = title;
        this.message = message;
        this.status = status;
        this.severity = severity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
