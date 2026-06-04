package com.factory.alert.infrastructure.repository;

import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.dto.response.CountResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlertRepositorySupport {

    Page<AlertResponse> findWithCondition(String status, String severity,
        Pageable pageable);

    CountResponse getCount(List<String> status);

    long bulkUpdateAll(String status, String severity);
}