package com.factory.alert.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.factory.alert.infrastructure.kafka.dto.AnomalyDetectedEvent;
import com.factory.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnomalyDetectedConsumer {

    private final ObjectMapper objectMapper;
    private final AlertService alertService;

    @KafkaListener(
            topics = "anomaly.detected",
            groupId = "notification-service"
    )
    public void consume(String message) {
        try {
            AnomalyDetectedEvent event = objectMapper.readValue(message, AnomalyDetectedEvent.class);

            log.info("[Kafka] anomaly.detected 이벤트 수신: logId={}, equipmentId={}, severity={}",
                    event.getLogId(),
                    event.getEquipmentId(),
                    event.getSeverity());

            if (event.getLogId() == null) {
                log.warn("[Kafka] logId가 없어 알림을 생성하지 않습니다. message={}", message);
                return;
            }

            if (event.getSeverity() == null) {
                log.warn("[Kafka] severity가 없어 알림을 생성하지 않습니다. logId={}", event.getLogId());
                return;
            }

            alertService.createAlert(
                    event.getLogId(),
                    createTitle(event),
                    createMessage(event),
                    event.getSeverity(),
                    event.getEquipmentId(),
                    event.getRecipeParameter(),
                    event.getRuleName(),
                    event.getAnomalyType()
            );

        } catch (Exception e) {
            log.error("[Kafka] anomaly.detected 이벤트 처리 실패. message={}", message, e);
        }
    }

    private String createTitle(AnomalyDetectedEvent event) {
        return "[" + event.getSeverity().toUpperCase() + "] 설비 "
                + event.getEquipmentId()
                + " 이상 감지";
    }

    private String createMessage(AnomalyDetectedEvent event) {
        return "설비 "
                + event.getEquipmentId()
                + "의 "
                + event.getRecipeParameter()
                + " 항목에서 "
                + event.getRuleName()
                + " 규칙에 의해 "
                + event.getAnomalyType()
                + " 이상이 감지되었습니다.";
    }
}