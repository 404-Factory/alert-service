package com.factory.alert.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AlertCreateRequest {

    private Long logId;
    private String title;
    private String message;
    private String severity;

    private String equipmentId;
    private String recipeParameter;
    private String ruleName;
    private String anomalyType;
}