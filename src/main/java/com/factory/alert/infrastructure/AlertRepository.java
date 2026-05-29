package com.factory.alert.infrastructure;

import com.factory.alert.domain.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    @Query("""
        SELECT a
        FROM Alert a
        WHERE (:severity IS NULL OR a.severity = :severity)
          AND (:isRead IS NULL OR a.isRead = :isRead)
        ORDER BY a.createdTime DESC
    """)
    Page<Alert> findAlerts(
            @Param("severity") String severity,
            @Param("isRead") Boolean isRead,
            Pageable pageable
    );

    long countByIsReadFalse();

    long countByIsReadFalseAndSeverity(String severity);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.isRead = false")
    int markAllAsRead();

    // 설비별 미확인 알림 심각도 판단용
    boolean existsByEquipmentIdAndSeverityAndIsReadFalse(String equipmentId, String severity);
}