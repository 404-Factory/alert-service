package com.factory.alert.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.factory.alert.config.JpaAuditingConfig;
import com.factory.alert.config.QueryDSLConfig;
import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.dto.response.CountResponse;
import com.factory.alert.infrastructure.entity.Alert;
import com.factory.alert.infrastructure.enums.AlertSeverity;
import com.factory.alert.infrastructure.enums.AlertStatus;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import({
    QueryDSLConfig.class,
    JpaAuditingConfig.class
})
class AlertRepositorySupportImplTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AlertRepositorySupportImpl alertRepositorySupport;

    @Test
    @DisplayName("상태와 심각도 조건으로 알림 목록을 조회한다")
    void findWithCondition() {
        persistAlert(
            10L,
            "EQ-001",
            "온도 이상",
            "온도가 임계치를 초과했습니다.",
            AlertStatus.UNREAD,
            AlertSeverity.WARNING
        );
        persistAlert(
            11L,
            "EQ-002",
            "압력 이상",
            "압력이 임계치를 초과했습니다.",
            AlertStatus.READ,
            AlertSeverity.CRITICAL
        );
        flushAndClear();

        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

        Page<AlertResponse> result =
            alertRepositorySupport.findWithCondition("unread", "warning", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAnomalyId()).isEqualTo(10L);
        assertThat(result.getContent().get(0).getEquipmentId()).isEqualTo("EQ-001");
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("온도 이상");
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("UNREAD");
        assertThat(result.getContent().get(0).getSeverity()).isEqualTo("WARNING");
    }

    @Test
    @DisplayName("상태 조건에 따라 전체, warning, critical 카운트를 조회한다")
    void getCount() {
        persistAlert(
            10L,
            "EQ-001",
            "온도 이상",
            "온도가 임계치를 초과했습니다.",
            AlertStatus.UNREAD,
            AlertSeverity.WARNING
        );
        persistAlert(
            11L,
            "EQ-002",
            "압력 이상",
            "압력이 임계치를 초과했습니다.",
            AlertStatus.UNREAD,
            AlertSeverity.CRITICAL
        );
        persistAlert(
            12L,
            "EQ-003",
            "진동 이상",
            "진동이 임계치를 초과했습니다.",
            AlertStatus.READ,
            AlertSeverity.CRITICAL
        );
        flushAndClear();

        CountResponse result = alertRepositorySupport.getCount(List.of("unread"));

        assertThat(result.getTotalCount()).isEqualTo(2L);
        assertThat(result.getWarningCount()).isEqualTo(1L);
        assertThat(result.getCriticalCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("전체 알림 상태를 bulk update 한다")
    void bulkUpdateAll_status() {
        persistAlert(
            10L,
            "EQ-001",
            "온도 이상",
            "온도가 임계치를 초과했습니다.",
            AlertStatus.UNREAD,
            AlertSeverity.WARNING
        );
        persistAlert(
            11L,
            "EQ-002",
            "압력 이상",
            "압력이 임계치를 초과했습니다.",
            AlertStatus.UNREAD,
            AlertSeverity.CRITICAL
        );
        flushAndClear();

        long updatedCount = alertRepositorySupport.bulkUpdateAll("read", null);
        flushAndClear();

        List<Alert> alerts = entityManager
            .createQuery("select a from Alert a order by a.id asc", Alert.class)
            .getResultList();

        assertThat(updatedCount).isEqualTo(2L);
        assertThat(alerts)
            .extracting(Alert::getStatus)
            .containsExactly(AlertStatus.READ, AlertStatus.READ);
    }

    private void persistAlert(
        Long anomalyId,
        String equipmentId,
        String title,
        String message,
        AlertStatus status,
        AlertSeverity severity
    ) {
        Alert alert = instantiateAlert();

        ReflectionTestUtils.setField(alert, "anomalyId", anomalyId);
        ReflectionTestUtils.setField(alert, "equipmentId", equipmentId);
        ReflectionTestUtils.setField(alert, "title", title);
        ReflectionTestUtils.setField(alert, "message", message);
        ReflectionTestUtils.setField(alert, "status", status);
        ReflectionTestUtils.setField(alert, "severity", severity);

        entityManager.persist(alert);
    }

    private Alert instantiateAlert() {
        try {
            Constructor<Alert> constructor = Alert.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Alert 테스트 객체 생성에 실패했습니다.", e);
        }
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
