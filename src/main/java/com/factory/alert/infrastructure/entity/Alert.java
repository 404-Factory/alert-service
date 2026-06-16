package com.factory.alert.infrastructure.entity;

import com.factory.alert.infrastructure.enums.AlertSeverity;
import com.factory.alert.infrastructure.enums.AlertStatus;
import com.factory.alert.event.payload.consumer.AnomalyCreatedPayload;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alerts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Alert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anomaly_id", nullable = false)
    private Long anomalyId;

    // 필요한지 잘 모르겠네..? 일단 보류, front 요청사항 따라 필요할 수도?
    // nullable false 여야함? 체크하자
    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "message", length = 500, nullable = false)
    private String message;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AlertStatus status;

    @Column(name = "severity", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AlertSeverity severity;

    public static Alert create(AnomalyCreatedPayload payload) {
        Alert alert = new Alert();
        alert.anomalyId = payload.getAnomalyId();
        alert.equipmentId = payload.getEquipmentId();
        alert.title = "[" + payload.getSeverity() + "] " + payload.getRecipeParameter() + " - " + payload.getCauseRule() + " 이상 감지";
        alert.message = "[" + payload.getRecipeParameter() + "] " + payload.getDetectionReason();
        alert.status = AlertStatus.UNREAD;
        alert.severity = AlertSeverity.fromCode(payload.getSeverity());
        return alert;
    }

    public void changeAlert(Long id, String status, String severity) {
        this.id = id != null ? id : this.id;
        this.status = status != null ? AlertStatus.fromCode(status) : this.status;
        this.severity = severity != null ? AlertSeverity.fromCode(severity) : this.severity;
    }
}
