package com.factory.alert.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ALERT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "log_id")
    private Long logId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "message", length = 500, nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "severity", length = 30, nullable = false)
    private String severity;

    @Column(name = "occurrence_count", nullable = false)
    private Integer occurrenceCount;

    @Column(name = "last_detected_at", nullable = false)
    private LocalDateTime lastDetectedAt;

    @PrePersist
    public void prePersist() {
        if (this.isRead == null) {
            this.isRead = false;
        }

        if (this.createdTime == null) {
            this.createdTime = LocalDateTime.now();
        }

        if (this.occurrenceCount == null) {
            this.occurrenceCount = 1;
        }

        if (this.lastDetectedAt == null) {
            this.lastDetectedAt = LocalDateTime.now();
        }
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void updateDuplicateAlert(String severity) {
        this.severity = severity;
        this.occurrenceCount += 1;
        this.lastDetectedAt = LocalDateTime.now();
        this.isRead = false;
    }
}