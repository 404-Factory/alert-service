package com.factory.alert.event.payload.consumer;

import com.factory.common.event.domain.EventPayload;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Payload of the {@code AnomalyCreated} event produced by anomaly-service.
 *
 * <p>
 * Field names must match the anomaly-service payload contract. {@code anomalyLogId} is the id of the
 * persisted anomaly log; anomaly-service must include it in the published payload.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnomalyCreatedPayload implements EventPayload {

    private Long anomalyLogId;
    private Long equipmentId;
    private String equipmentName;
    private String recipeParameter;
    private String severity;
    private Instant occurredTime;
    private String causeRule;
    private String detectionReason;
}
