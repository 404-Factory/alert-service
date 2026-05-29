package com.factory.alert.service;

import com.factory.alert.domain.dto.AlertDetailResponse;
import com.factory.alert.domain.dto.AlertResponse;
import com.factory.alert.domain.dto.UnreadCountResponse;
import com.factory.alert.domain.entity.Alert;
import com.factory.alert.domain.entity.AlertAnomalyLog;
import com.factory.alert.domain.entity.AnomalyLog;
import com.factory.alert.infrastructure.AlertAnomalyLogRepository;
import com.factory.alert.infrastructure.AlertRepository;
import com.factory.alert.infrastructure.AnomalyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertDuplicateService alertDuplicateService;
    private final AnomalyLogRepository anomalyLogRepository;
    private final AlertAnomalyLogRepository alertAnomalyLogRepository;

    private static final int COMPOSITE_WINDOW_MINUTES = 5;

    // 최소 데이터 수 기준
    // 팀에서 기준값 정하면 이 숫자만 바꾸면 됨
    private static final int MIN_SAMPLE_COUNT = 1;

    // 복합 이상 중복 방지용 Redis key 구성값
    private static final String COMPOSITE_RECIPE_PARAMETER = "COMPOSITE_SENSOR";
    private static final String COMPOSITE_RULE_NAME = "COMPOSITE_DIFFERENT_SENSOR";
    private static final String COMPOSITE_ANOMALY_TYPE = "MULTI_SENSOR";

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

    // 설비별 미확인 알림 심각도 조회
    // CRITICAL 미확인 알림이 하나라도 있으면 CRITICAL
    // CRITICAL은 없고 WARNING 미확인 알림이 있으면 WARNING
    // 미확인 알림이 없으면 NORMAL
    public String getUnreadEquipmentAlertSeverity(String equipmentId) {
        boolean hasCritical = alertRepository.existsByEquipmentIdAndSeverityAndIsReadFalse(
                equipmentId,
                "CRITICAL"
        );

        if (hasCritical) {
            return "CRITICAL";
        }

        boolean hasWarning = alertRepository.existsByEquipmentIdAndSeverityAndIsReadFalse(
                equipmentId,
                "WARNING"
        );

        if (hasWarning) {
            return "WARNING";
        }

        return "NORMAL";
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
    // 단일 CAUTION은 ALERT 생성 X
    // 같은 설비에서 서로 다른 센서의 CAUTION 이상이 최근 5분 내 함께 발생하면 WARNING으로 상향 생성
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
        if (severity == null) {
            return null;
        }

        AnomalyLog currentLog = anomalyLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("이상 로그를 찾을 수 없습니다. logId=" + logId));

        String finalSeverity = severity.toUpperCase();
        String finalTitle = title;
        String finalMessage = message;

        List<AnomalyLog> relatedLogs = new ArrayList<>();

        String duplicateRecipeParameter = recipeParameter;
        String duplicateRuleName = ruleName;
        String duplicateAnomalyType = anomalyType;

        // 1. CAUTION이면 바로 버리지 않고, 복합 이상인지 먼저 판단
        if ("CAUTION".equalsIgnoreCase(severity)) {
            relatedLogs = findCompositeRelatedLogs(currentLog, equipmentId, recipeParameter, logId);

            // 최근 5분 내 같은 설비의 다른 센서 CAUTION 이상이 없으면 단일 CAUTION이므로 알림 생성 X
            if (relatedLogs.isEmpty()) {
                return null;
            }

            // 복합 이상이면 WARNING으로 상향
            finalSeverity = "WARNING";
            finalTitle = createCompositeWarningTitle(equipmentId);
            finalMessage = createCompositeWarningMessage(currentLog, relatedLogs);

            // 복합 이상은 특정 센서 하나 기준이 아니라 복합 알림 기준으로 중복 방지
            duplicateRecipeParameter = COMPOSITE_RECIPE_PARAMETER;
            duplicateRuleName = COMPOSITE_RULE_NAME;
            duplicateAnomalyType = COMPOSITE_ANOMALY_TYPE;
        }

        // 2. 최종적으로 ALERT는 WARNING 또는 CRITICAL만 생성 가능
        if (
                !finalSeverity.equalsIgnoreCase("WARNING") &&
                !finalSeverity.equalsIgnoreCase("CRITICAL")
        ) {
            throw new IllegalArgumentException("ALERT는 WARNING 또는 CRITICAL만 생성할 수 있습니다.");
        }

        // 3. Redis 중복 알림 확인
        Long existingAlertId = alertDuplicateService.getExistingAlertId(
                equipmentId,
                duplicateRecipeParameter,
                duplicateRuleName,
                duplicateAnomalyType
        );

        if (existingAlertId != null) {
            Alert existingAlert = alertRepository.findById(existingAlertId)
                    .orElseThrow(() -> new IllegalArgumentException("기존 알림을 찾을 수 없습니다."));

            existingAlert.updateDuplicateAlert(finalSeverity.toUpperCase());

            // 기존 알림에도 새 이상로그 근거를 연결
            saveAlertAnomalyLogMappings(existingAlert, currentLog, relatedLogs);

            return existingAlert;
        }

        // 4. 신규 ALERT 생성
        Alert alert = Alert.builder()
                .logId(logId)
                .equipmentId(equipmentId)
                .title(finalTitle)
                .message(finalMessage)
                .severity(finalSeverity.toUpperCase())
                .isRead(false)
                .build();

        Alert savedAlert = alertRepository.save(alert);

        // 5. ALERT와 ANOMALY_LOG 매핑 저장
        saveAlertAnomalyLogMappings(savedAlert, currentLog, relatedLogs);

        // 6. Redis cooldown key 저장
        alertDuplicateService.saveCooldownKey(
                equipmentId,
                duplicateRecipeParameter,
                duplicateRuleName,
                duplicateAnomalyType,
                savedAlert.getAlertId(),
                finalSeverity
        );

        return savedAlert;
    }

    private List<AnomalyLog> findCompositeRelatedLogs(
            AnomalyLog currentLog,
            String equipmentId,
            String recipeParameter,
            Long logId
    ) {
        LocalDateTime endTime = currentLog.getOccurredTime();

        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        LocalDateTime startTime = endTime.minusMinutes(COMPOSITE_WINDOW_MINUTES);

        Long equipmentIdValue = convertEquipmentId(equipmentId);

        return anomalyLogRepository.findRecentDifferentSensorCautionLogs(
                equipmentIdValue,
                recipeParameter,
                startTime,
                endTime,
                logId,
                MIN_SAMPLE_COUNT
        );
    }

    private Long convertEquipmentId(String equipmentId) {
        try {
            return Long.valueOf(equipmentId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("equipmentId는 숫자 형태여야 합니다. equipmentId=" + equipmentId);
        }
    }

    private String createCompositeWarningTitle(String equipmentId) {
        return "[WARNING] 설비 " + equipmentId + " 복합 이상 감지";
    }

    private String createCompositeWarningMessage(AnomalyLog currentLog, List<AnomalyLog> relatedLogs) {
        StringBuilder sb = new StringBuilder();

        sb.append("동일 설비에서 최근 5분 내 서로 다른 센서 항목의 CAUTION 이상이 동시에 감지되어 WARNING 알림으로 상향되었습니다.");

        sb.append(" 현재 이상: ");
        sb.append(currentLog.getRecipeParameter());
        sb.append(" / ");
        sb.append(currentLog.getRuleName());

        sb.append(" 관련 이상: ");

        for (AnomalyLog relatedLog : relatedLogs) {
            sb.append("[");
            sb.append(relatedLog.getRecipeParameter());
            sb.append(" / ");
            sb.append(relatedLog.getRuleName());
            sb.append("] ");
        }

        return sb.toString();
    }

    private void saveAlertAnomalyLogMappings(
            Alert alert,
            AnomalyLog currentLog,
            List<AnomalyLog> relatedLogs
    ) {
        saveMappingIfNotExists(alert, currentLog, "PRIMARY");

        for (AnomalyLog relatedLog : relatedLogs) {
            saveMappingIfNotExists(alert, relatedLog, "RELATED");
        }
    }

    private void saveMappingIfNotExists(Alert alert, AnomalyLog anomalyLog, String relationType) {
        boolean exists = alertAnomalyLogRepository.existsByAlertAlertIdAndAnomalyLogLogId(
                alert.getAlertId(),
                anomalyLog.getLogId()
        );

        if (exists) {
            return;
        }

        alertAnomalyLogRepository.save(
                AlertAnomalyLog.builder()
                        .alert(alert)
                        .anomalyLog(anomalyLog)
                        .relationType(relationType)
                        .build()
        );
    }
}