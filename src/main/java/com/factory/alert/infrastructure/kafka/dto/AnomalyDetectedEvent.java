package com.factory.alert.infrastructure.kafka.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnomalyDetectedEvent {

    private Long logId;

    private String equipmentId;

    private String recipeParameter;

    private String ruleName;

    private String anomalyType;

    private String severity;

    private String occurredTime;
}