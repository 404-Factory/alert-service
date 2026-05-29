package com.factory.alert.infrastructure;

import com.factory.alert.domain.entity.AnomalyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AnomalyLogRepository extends JpaRepository<AnomalyLog, Long> {

    @Query("""
        SELECT a
        FROM AnomalyLog a
        WHERE a.equipmentId = :equipmentId
          AND a.recipeParameter <> :recipeParameter
          AND a.severity = 'CAUTION'
          AND a.occurredTime BETWEEN :startTime AND :endTime
          AND a.logId <> :currentLogId
          AND a.sampleCount >= :minSampleCount
    """)
    List<AnomalyLog> findRecentDifferentSensorCautionLogs(
            @Param("equipmentId") Long equipmentId,
            @Param("recipeParameter") String recipeParameter,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("currentLogId") Long currentLogId,
            @Param("minSampleCount") int minSampleCount
    );
}