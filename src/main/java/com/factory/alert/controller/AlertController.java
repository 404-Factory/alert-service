package com.factory.alert.controller;

import com.factory.alert.domain.dto.AlertCreateRequest;
import com.factory.alert.domain.dto.AlertDetailResponse;
import com.factory.alert.domain.dto.AlertResponse;
import com.factory.alert.domain.dto.UnreadCountResponse;
import com.factory.alert.domain.entity.Alert;
import com.factory.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(
        originPatterns = {
                "http://localhost:*",
                "http://127.0.0.1:*"
        },
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PATCH,
                RequestMethod.OPTIONS
        }
)
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // 알림 목록 조회
    @GetMapping
    public Page<AlertResponse> getAlerts(
         @RequestParam(required = false, defaultValue = "ALL") String severity,
         @RequestParam(required = false) Boolean isRead,
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size
    ) {
        return alertService.getAlerts(severity, isRead, page, size);
    }

    // 알림 생성
    // CAUTION은 ALERT 생성 X
    // WARNING, CRITICAL만 ALERT 생성
    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody AlertCreateRequest request) {
        Alert alert = alertService.createAlert(
                request.getLogId(),
                request.getTitle(),
                request.getMessage(),
                request.getSeverity(),
                request.getEquipmentId(),
                request.getRecipeParameter(),
                request.getRuleName(),
                request.getAnomalyType()
        );

        if (alert == null) {
            return ResponseEntity.ok("CAUTION은 ALERT를 생성하지 않습니다.");
        }

        return ResponseEntity.ok(AlertResponse.from(alert));
    }

    // Header 알림 카운트 조회
    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount() {
        return alertService.getUnreadCount();
    }

    // 전체 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<Void> readAllAlerts() {
        alertService.readAllAlerts();
        return ResponseEntity.noContent().build();
    }

    // 단건 읽음 처리
    @PatchMapping("/{alertId}/read")
    public ResponseEntity<Void> readAlert(@PathVariable Long alertId) {
        alertService.readAlert(alertId);
        return ResponseEntity.noContent().build();
    }

    // 알림 상세 조회
    @GetMapping("/{alertId}")
    public AlertDetailResponse getAlertDetail(@PathVariable Long alertId) {
        return alertService.getAlertDetail(alertId);
    }
}