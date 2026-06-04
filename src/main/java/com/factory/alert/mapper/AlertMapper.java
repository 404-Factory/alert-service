package com.factory.alert.mapper;

import com.factory.alert.dto.request.AlertUpdateRequest;
import com.factory.alert.dto.response.AlertResponse;
import com.factory.alert.infrastructure.entity.Alert;
import com.factory.alert.infrastructure.enums.AlertSeverity;
import com.factory.alert.infrastructure.enums.AlertStatus;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlertMapper {


    AlertResponse toAlertResponse(Alert alert);

    default AlertStatus toAlertStatus(String status) {
        return AlertStatus.fromCode(status);
    }

    default String toStatusCode(AlertStatus status) {
        return status == null ? null : status.getCode();
    }

    default AlertSeverity toAlertSeverity(String severity) {
        return AlertSeverity.fromCode(severity);
    }

    default String toSeverityCode(AlertSeverity severity) {
        return severity == null ? null : severity.getCode();
    }
}
