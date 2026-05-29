package com.factory.alert.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AlertDuplicateService {

    private final StringRedisTemplate redisTemplate;

    // 기존 중복 ALERT ID 조회
    public Long getExistingAlertId(
            String equipmentId,
            String recipeParameter,
            String ruleName,
            String anomalyType
    ) {
        String key = createAlertKey(equipmentId, recipeParameter, ruleName, anomalyType);
        String alertId = redisTemplate.opsForValue().get(key);

        if (alertId == null) {
            return null;
        }

        return Long.valueOf(alertId);
    }

    // 신규 ALERT 생성 후 Redis cooldown key 저장
    public void saveCooldownKey(
            String equipmentId,
            String recipeParameter,
            String ruleName,
            String anomalyType,
            Long alertId,
            String severity
    ) {
        String key = createAlertKey(equipmentId, recipeParameter, ruleName, anomalyType);
        Duration ttl = getTtlBySeverity(severity);

        redisTemplate.opsForValue().set(
                key,
                String.valueOf(alertId),
                ttl
        );
    }

    private String createAlertKey(
            String equipmentId,
            String recipeParameter,
            String ruleName,
            String anomalyType
    ) {
        return "alert:" + equipmentId + ":" + recipeParameter + ":" + ruleName + ":" + anomalyType;
    }

    private Duration getTtlBySeverity(String severity) {
        if (severity == null) {
            return Duration.ofMinutes(5);
        }

        return switch (severity.toUpperCase()) {
            case "WARNING" -> Duration.ofMinutes(5);
            case "CRITICAL" -> Duration.ofMinutes(3);
            default -> Duration.ofMinutes(5);
        };
    }
}