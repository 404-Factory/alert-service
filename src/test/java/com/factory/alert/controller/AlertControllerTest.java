package com.factory.alert.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.dto.response.CountResponse;
import com.factory.alert.exception.AlertErrorCode;
import com.factory.alert.exception.AlertException;
import com.factory.alert.service.AlertService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("알림 컨트롤러 테스트")
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertService alertService;

    @Test
    @DisplayName("알림 목록을 조회한다")
    void getAlerts() throws Exception {
        AlertResponse response = AlertResponse.builder()
            .id(1L)
            .anomalyId(10L)
            .equipmentId(1L)
            .title("온도 이상")
            .message("온도가 임계치를 초과했습니다.")
            .status("UNREAD")
            .severity("WARNING")
            .build();

        when(alertService.getAllAlerts(eq("unread"), eq("warning"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/alerts")
                .param("status", "unread")
                .param("severity", "warning"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.content[0].id").value(1L))
            .andExpect(jsonPath("$.data.content[0].anomalyId").value(10L))
            .andExpect(jsonPath("$.data.content[0].equipmentId").value(1L))
            .andExpect(jsonPath("$.data.content[0].title").value("온도 이상"))
            .andExpect(jsonPath("$.data.content[0].message").value("온도가 임계치를 초과했습니다."))
            .andExpect(jsonPath("$.data.content[0].status").value("UNREAD"))
            .andExpect(jsonPath("$.data.content[0].severity").value("WARNING"));

        verify(alertService).getAllAlerts(eq("unread"), eq("warning"), any(Pageable.class));
    }

    @Test
    @DisplayName("알림 단건을 조회한다")
    void getAlert() throws Exception {
        AlertResponse response = AlertResponse.builder()
            .id(1L)
            .anomalyId(10L)
            .equipmentId(1L)
            .title("온도 이상")
            .message("온도가 임계치를 초과했습니다.")
            .status("UNREAD")
            .severity("WARNING")
            .build();

        when(alertService.getAlert(1L)).thenReturn(response);

        mockMvc.perform(get("/api/alerts/{id}", 1L))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.anomalyId").value(10L))
            .andExpect(jsonPath("$.data.equipmentId").value(1L))
            .andExpect(jsonPath("$.data.title").value("온도 이상"))
            .andExpect(jsonPath("$.data.message").value("온도가 임계치를 초과했습니다."))
            .andExpect(jsonPath("$.data.status").value("UNREAD"))
            .andExpect(jsonPath("$.data.severity").value("WARNING"));

        verify(alertService).getAlert(1L);
    }

    @Test
    @DisplayName("상태 조건에 따른 알림 카운트를 조회한다")
    void getCount() throws Exception {
        CountResponse response = new CountResponse(10L, 7L, 3L);

        when(alertService.getCount(List.of("unread"))).thenReturn(response);

        mockMvc.perform(get("/api/alerts/count")
                .param("status", "unread"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.totalCount").value(10L))
            .andExpect(jsonPath("$.data.warningCount").value(7L))
            .andExpect(jsonPath("$.data.criticalCount").value(3L));

        verify(alertService).getCount(List.of("unread"));
    }

    @Test
    @DisplayName("전체 알림을 수정한다")
    void updateAlerts() throws Exception {
        String requestBody = """
            {
              "status": "read",
              "severity": "warning"
            }
            """;

        mockMvc.perform(patch("/api/alerts")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isNoContent());

        verify(alertService).updateAllAlerts("read", "warning");
    }

    @Test
    @DisplayName("단건 알림을 수정한다")
    void updateAlert() throws Exception {
        String requestBody = """
            {
              "status": "read",
              "severity": "critical"
            }
            """;

        mockMvc.perform(patch("/api/alerts/{id}", 1L)
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isNoContent());

        verify(alertService).updateAlert(1L, "read", "critical");
    }

    @Test
    @DisplayName("존재하지 않는 알림 단건 조회 시 404를 반환한다")
    void getAlertThrowsAlertNotFound() throws Exception {
        when(alertService.getAlert(999L))
            .thenThrow(new AlertException(AlertErrorCode.ALERT_NOT_FOUND));

        mockMvc.perform(get("/api/alerts/{id}", 999L))
            .andExpect(status().isNotFound());

        verify(alertService).getAlert(999L);
    }

    @Test
    @DisplayName("존재하지 않는 알림 단건 수정 시 404를 반환한다")
    void updateAlertThrowsAlertNotFound() throws Exception {
        String requestBody = """
            {
              "status": "read",
              "severity": "critical"
            }
            """;

        doThrow(new AlertException(AlertErrorCode.ALERT_NOT_FOUND))
            .when(alertService)
            .updateAlert(999L, "read", "critical");

        mockMvc.perform(patch("/api/alerts/{id}", 999L)
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isNotFound());

        verify(alertService).updateAlert(999L, "read", "critical");
    }

    @Test
    @DisplayName("잘못된 상태로 알림 목록 조회 시 400을 반환한다")
    void getAlertsThrowsInvalidAlertStatus() throws Exception {
        mockMvc.perform(get("/api/alerts")
                .param("status", "invalid")
                .param("severity", "warning"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Invalid input value."))
            .andExpect(jsonPath("$.error.code").value("CA002"))
            .andExpect(jsonPath("$.error.details[0].target").value("status"))
            .andExpect(jsonPath("$.error.details[0].message").value(
                "status must be read, unread or null"));

        verifyNoInteractions(alertService);
    }
}
