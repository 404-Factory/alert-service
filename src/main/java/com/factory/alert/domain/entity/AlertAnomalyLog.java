package com.factory.alert.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "alert_anomaly_log",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_alert_anomaly_log",
                        columnNames = {"alert_id", "log_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlertAnomalyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_anomaly_log_id")
    private Long alertAnomalyLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private AnomalyLog anomalyLog;

    @Column(name = "relation_type", length = 30)
    private String relationType;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Builder
    public AlertAnomalyLog(Alert alert, AnomalyLog anomalyLog, String relationType) {
        this.alert = alert;
        this.anomalyLog = anomalyLog;
        this.relationType = relationType;
        this.createdTime = LocalDateTime.now();
    }
}