package com.factory.alert.infrastructure.enums;

import com.factory.alert.exception.AlertErrorCode;
import com.factory.alert.exception.AlertException;
import lombok.Getter;

@Getter
public enum AlertSeverity {
    WARNING("warning"),
    CRITICAL("critical");

    private final String code;

    AlertSeverity(String code) {
        this.code = code;
    }

    public static AlertSeverity fromCode(String code) {
        try {
            return AlertSeverity.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AlertException(AlertErrorCode.INVALID_ALERT_SEVERITY);
        }
    }
}
