package com.factory.alert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.dto.response.CountResponse;
import com.factory.alert.exception.AlertException;
import com.factory.alert.infrastructure.entity.Alert;
import com.factory.alert.infrastructure.enums.AlertSeverity;
import com.factory.alert.infrastructure.enums.AlertStatus;
import com.factory.alert.infrastructure.repository.AlertRepository;
import com.factory.alert.mapper.AlertMapper;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertMapper alertMapper;

    @InjectMocks
    private AlertServiceImpl alertService;

    @Test
    @DisplayName("조건에 맞는 알림 목록을 조회한다")
    void getAllAlerts() {
        PageRequest pageable = PageRequest.of(0, 10);

        AlertResponse response = AlertResponse.builder()
            .id(1L)
            .anomalyId(10L)
            .equipmentId("EQ-001")
            .title("온도 이상")
            .message("온도가 임계치를 초과했습니다.")
            .status("UNREAD")
            .severity("WARNING")
            .build();

        Page<AlertResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(alertRepository.findWithCondition("unread", "warning", pageable))
            .thenReturn(page);

        Page<AlertResponse> result = alertService.getAllAlerts("unread", "warning", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("UNREAD");
        assertThat(result.getContent().get(0).getSeverity()).isEqualTo("WARNING");

        verify(alertRepository).findWithCondition("unread", "warning", pageable);
    }

    @Test
    @DisplayName("알림 단건을 조회한다")
    void getAlert() {
        Alert alert = createAlert(
            1L,
            10L,
            "EQ-001",
            "온도 이상",
            "온도가 임계치를 초과했습니다.",
            AlertStatus.UNREAD,
            AlertSeverity.WARNING
        );

        AlertResponse response = AlertResponse.builder()
            .id(1L)
            .anomalyId(10L)
            .equipmentId("EQ-001")
            .title("온도 이상")
            .message("온도가 임계치를 초과했습니다.")
            .status("UNREAD")
            .severity("WARNING")
            .build();

        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertMapper.toAlertResponse(alert)).thenReturn(response);

        AlertResponse result = alertService.getAlert(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAnomalyId()).isEqualTo(10L);
        assertThat(result.getEquipmentId()).isEqualTo("EQ-001");
        assertThat(result.getTitle()).isEqualTo("온도 이상");
        assertThat(result.getMessage()).isEqualTo("온도가 임계치를 초과했습니다.");
        assertThat(result.getStatus()).isEqualTo("UNREAD");
        assertThat(result.getSeverity()).isEqualTo("WARNING");

        verify(alertRepository).findById(1L);
        verify(alertMapper).toAlertResponse(alert);
    }

    @Test
    @DisplayName("존재하지 않는 알림 단건 조회 시 예외가 발생한다")
    void getAlert_notFound() {
        when(alertRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.getAlert(1L))
            .isInstanceOf(AlertException.class);

        verify(alertRepository).findById(1L);
    }

    @Test
    @DisplayName("상태 조건에 따른 알림 카운트를 조회한다")
    void getCount() {
        CountResponse response = new CountResponse(10L, 7L, 3L);

        when(alertRepository.getCount(List.of("unread"))).thenReturn(response);

        CountResponse result = alertService.getCount(List.of("unread"));

        assertThat(result.getTotalCount()).isEqualTo(10L);
        assertThat(result.getWarningCount()).isEqualTo(7L);
        assertThat(result.getCriticalCount()).isEqualTo(3L);

        verify(alertRepository).getCount(List.of("unread"));
    }

    @Test
    @DisplayName("전체 알림을 수정한다")
    void updateAllAlerts() {
        when(alertRepository.bulkUpdateAll("read", "warning")).thenReturn(3L);

        alertService.updateAllAlerts("read", "warning");

        verify(alertRepository).bulkUpdateAll("read", "warning");
    }

    @Test
    @DisplayName("단건 알림을 수정한다")
    void updateAlert() {
        Alert alert = createAlert(
            1L,
            10L,
            "EQ-001",
            "온도 이상",
            "온도가 임계치를 초과했습니다.",
            AlertStatus.UNREAD,
            AlertSeverity.WARNING
        );

        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));

        alertService.updateAlert(1L, "read", "critical");

        assertThat(alert.getId()).isEqualTo(1L);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.READ);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.CRITICAL);

        verify(alertRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 알림 수정 시 예외가 발생한다")
    void updateAlert_notFound() {
        when(alertRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.updateAlert(1L, "read", "critical"))
            .isInstanceOf(AlertException.class);

        verify(alertRepository).findById(1L);
    }

    private Alert createAlert(
        Long id,
        Long anomalyId,
        String equipmentId,
        String title,
        String message,
        AlertStatus status,
        AlertSeverity severity
    ) {
        Alert alert = instantiateAlert();

        ReflectionTestUtils.setField(alert, "id", id);
        ReflectionTestUtils.setField(alert, "anomalyId", anomalyId);
        ReflectionTestUtils.setField(alert, "equipmentId", equipmentId);
        ReflectionTestUtils.setField(alert, "title", title);
        ReflectionTestUtils.setField(alert, "message", message);
        ReflectionTestUtils.setField(alert, "status", status);
        ReflectionTestUtils.setField(alert, "severity", severity);

        return alert;
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
}
