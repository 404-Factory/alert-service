package com.factory.alert.service;

import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.dto.response.CountResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlertService {

    Page<AlertResponse> getAllAlerts(String status, String severity, Pageable pageable);

    AlertResponse getAlert(Long id);

    CountResponse getCount(List<String> status);

    void updateAllAlerts(String status, String severity);

    void updateAlert(Long id, String status, String severity);
}