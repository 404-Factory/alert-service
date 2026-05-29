package com.factory.alert.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "anomaly_log")
@Getter
@NoArgsConstructor
public class AnomalyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "equipment_rec_id")
    private Long equipmentRecId;

    @Column(name = "recipe_parameter", length = 50)
    private String recipeParameter;

    @Column(name = "severity", length = 30)
    private String severity;

    @Column(name = "occurred_time")
    private LocalDateTime occurredTime;

    @Column(name = "rule_name", length = 50)
    private String ruleName;

    @Column(name = "anomaly_type", length = 50)
    private String anomalyType;

    @Column(name = "window_start_time")
    private LocalDateTime windowStartTime;

    @Column(name = "sample_count")
    private Integer sampleCount;

    @Column(name = "detection_reason", length = 500)
    private String detectionReason;
}