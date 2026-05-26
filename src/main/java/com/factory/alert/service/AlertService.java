package com.factory.alert.service;

import com.factory.alert.domain.dto.AlertDetailResponse;
import com.factory.alert.domain.dto.AlertResponse;
import com.factory.alert.domain.dto.UnreadCountResponse;
import com.factory.alert.domain.entity.Alert;
import com.factory.alert.infrastructure.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertDuplicateService alertDuplicateService;

    // 알림 목록 조회
    public Page<AlertResponse> getAlerts(String severity, Boolean isRead, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdTime")
        );

        String severityCondition = convertSeverity(severity);

        return alertRepository.findAlerts(severityCondition, isRead, pageable)
                .map(AlertResponse::from);
    }

    // severity는 알림 심각도 필터값
    private String convertSeverity(String severity) {
        if (severity == null || severity.equalsIgnoreCase("ALL")) {
            return null;
        }

        if (
                severity.equalsIgnoreCase("WARNING") ||
                severity.equalsIgnoreCase("CRITICAL")
        ) {
            return severity.toUpperCase();
        }

        throw new IllegalArgumentException("알림 심각도는 ALL, WARNING, CRITICAL만 가능합니다.");
    }

    // Header 알림 카운트 조회
    public UnreadCountResponse getUnreadCount() {
        long totalCount = alertRepository.countByIsReadFalse();
        long warningCount = alertRepository.countByIsReadFalseAndSeverity("WARNING");
        long criticalCount = alertRepository.countByIsReadFalseAndSeverity("CRITICAL");

        return new UnreadCountResponse(
                totalCount,
                warningCount,
                criticalCount
        );
    }

    // 전체 읽음 처리
    @Transactional
    public void readAllAlerts() {
        alertRepository.markAllAsRead();
    }

    // 단건 읽음 처리
    @Transactional
    public void readAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        alert.markAsRead();
    }

    // 알림 상세 조회
    public AlertDetailResponse getAlertDetail(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        return AlertDetailResponse.from(alert);
    }

    // 내부 알림 생성 로직
    // CAUTION은 ALERT 생성 X
    // WARNING, CRITICAL은 ALERT 생성 O
    // 동일 조건의 ALERT가 Redis에 존재하면 신규 생성하지 않고 기존 ALERT 갱신
    @Transactional
    public Alert createAlert(
            Long logId,
            String title,
            String message,
            String severity,
            String equipmentId,
            String recipeParameter,
            String ruleName,
            String anomalyType
    ) {
        if (severity == null || severity.equalsIgnoreCase("CAUTION")) {
            return null;
        }

        if (
                !severity.equalsIgnoreCase("WARNING") &&
                !severity.equalsIgnoreCase("CRITICAL")
        ) {
            throw new IllegalArgumentException("ALERT는 WARNING 또는 CRITICAL만 생성할 수 있습니다.");
        }

        Long existingAlertId = alertDuplicateService.getExistingAlertId(
                equipmentId,
                recipeParameter,
                ruleName,
                anomalyType
        );

        if (existingAlertId != null) {
            Alert existingAlert = alertRepository.findById(existingAlertId)
                    .orElseThrow(() -> new IllegalArgumentException("기존 알림을 찾을 수 없습니다."));

            existingAlert.updateDuplicateAlert(severity.toUpperCase());

            return existingAlert;
        }

        Alert alert = Alert.builder()
                .logId(logId)
                .equipmentId(equipmentId)
                .title(title)
                .message(message)
                .severity(severity.toUpperCase())
                .isRead(false)
                .build();

        Alert savedAlert = alertRepository.save(alert);

        alertDuplicateService.saveCooldownKey(
                equipmentId,
                recipeParameter,
                ruleName,
                anomalyType,
                savedAlert.getAlertId(),
                severity
        );

        return savedAlert;
    }
}