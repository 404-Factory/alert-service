package com.factory.alert.controller;

import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.dto.response.CountResponse;
import com.factory.alert.dto.request.AlertSearchCondition;
import com.factory.alert.dto.request.AlertUpdateRequest;
import com.factory.alert.service.AlertService;
import com.factory.alert.validation.constraints.ValidStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // 알림 전체 조회
    @GetMapping
    public Page<AlertResponse> getAlerts(
        @Valid @ModelAttribute AlertSearchCondition condition,
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String status = condition.getStatus();
        String severity = condition.getSeverity();
        log.info("status: {}", status);
        log.info("severity: {}", severity);
        return alertService.getAllAlerts(status, severity, pageable);
    }

    // count => status 따라 구분
    @GetMapping("/count")
    public ResponseEntity<CountResponse> getCount(
        @RequestParam(name = "status", required = false) List<@ValidStatus String> status) {
        if (status != null) {
            log.info("status: {}", status.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]")));
        } else {
            log.info("status: null");
        }
        return ResponseEntity.ok(alertService.getCount(status));
    }

    // 알림 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlert(
        @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(alertService.getAlert(id));
    }

    // 전체 읽음 처리
    @PatchMapping
    public ResponseEntity<Void> updateAlerts(@Valid @RequestBody AlertUpdateRequest request) {
        String status = request.getStatus();
        String severity = request.getSeverity();
        log.info("status: {}", status);
        log.info("severity: {}", severity);
        alertService.updateAllAlerts(status, severity);
        return ResponseEntity.noContent().build();
    }

    // 단건 읽음 처리
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateAlert(@PathVariable(name = "id") Long id,
        @Valid @RequestBody AlertUpdateRequest request) {
        String status = request.getStatus();
        String severity = request.getSeverity();
        log.info("status: {}", status);
        log.info("severity: {}", severity);
        alertService.updateAlert(id, status, severity);
        return ResponseEntity.noContent().build();
    }
}