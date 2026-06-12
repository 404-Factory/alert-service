package com.factory.alert.kafka.consumer;

import com.factory.alert.infrastructure.entity.Alert;
import com.factory.alert.infrastructure.repository.AlertRepository;
import com.factory.alert.kafka.dto.AnomalyCreatedPayload;
import com.factory.common.event.domain.Event;
import com.factory.common.inbox.jpa.aop.InboxProcessed;
import com.factory.common.kafka.support.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnomalyCreatedEventHandler implements EventHandler<AnomalyCreatedPayload> {

    private final AlertRepository alertRepository;

    @Override
    public String getEventType() {
        return "AnomalyCreated";
    }

    @Override
    @Transactional
    @InboxProcessed
    public void process(Event<AnomalyCreatedPayload> event) {
        AnomalyCreatedPayload payload = event.getPayload();

        if ("CAUTION".equalsIgnoreCase(payload.getSeverity())) {
            log.debug("Skipping CAUTION severity anomaly: anomalyLogId={}", payload.getAnomalyLogId());
            return;
        }

        Alert alert = Alert.create(payload);
        alertRepository.save(alert);
        log.info("Alert created: anomalyLogId={}, severity={}",
            payload.getAnomalyLogId(), payload.getSeverity());
    }
}
