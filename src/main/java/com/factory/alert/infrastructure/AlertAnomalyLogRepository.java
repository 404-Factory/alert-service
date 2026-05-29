package com.factory.alert.infrastructure;

import com.factory.alert.domain.entity.AlertAnomalyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertAnomalyLogRepository extends JpaRepository<AlertAnomalyLog, Long> {

    List<AlertAnomalyLog> findByAlertAlertId(Long alertId);

    boolean existsByAlertAlertIdAndAnomalyLogLogId(Long alertId, Long logId);
}