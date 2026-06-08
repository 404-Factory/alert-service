package com.factory.alert.infrastructure.enums;

import com.factory.alert.exception.AlertErrorCode;
import com.factory.alert.exception.AlertException;
import lombok.Getter;

@Getter
public enum AlertStatus {
    UNREAD("unread"),
    READ("read");

    private final String code;

    AlertStatus(String code) {
        this.code = code;
    }

    public static AlertStatus fromCode(String code) {
        try {
            return AlertStatus.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AlertException(AlertErrorCode.INVALID_ALERT_STATUS);
        }
    }
}
