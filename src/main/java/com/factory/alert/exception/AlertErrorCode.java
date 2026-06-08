package com.factory.alert.exception;

import com.factory.common.core.exception.ErrorCode;

public enum AlertErrorCode implements ErrorCode {
    INVALID_ALERT_STATUS(400, "AL001", "Invalid status value."),
    INVALID_ALERT_SEVERITY(400, "AL002", "Invalid severity value."),
    ALERT_NOT_FOUND(404, "AL003", "Alert not found.");

    private final int status;
    private final String code;
    private final String message;

    AlertErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
