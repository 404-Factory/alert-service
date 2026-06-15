package com.factory.alert.service;

import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.dto.response.CountResponse;
import com.factory.alert.exception.AlertErrorCode;
import com.factory.alert.exception.AlertException;
import com.factory.alert.infrastructure.entity.Alert;
import com.factory.alert.infrastructure.repository.AlertRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AlertResponse> getAllAlerts(String status, String severity, Pageable pageable) {

        return alertRepository.fetchAlertsWithCondition(status, severity, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertResponse getAlert(Long id) {

        AlertResponse response = alertRepository.fetchAlert(id);
        if (response == null) {
            throw new AlertException(AlertErrorCode.ALERT_NOT_FOUND);
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CountResponse getCount(List<String> status) {

        return alertRepository.fetchCountWithStatus(status);
    }

    @Override
    @Transactional
    public void updateAllAlerts(String status, String severity) {

        alertRepository.bulkUpdateAll(status, severity);
    }

    @Override
    @Transactional
    public void updateAlert(Long id, String status, String severity) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new AlertException(AlertErrorCode.ALERT_NOT_FOUND));

        alert.changeAlert(id, status, severity);
    }
}
