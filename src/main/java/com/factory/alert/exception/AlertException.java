package com.factory.alert.exception;

import com.factory.common.core.exception.BaseException;
import com.factory.common.core.exception.ErrorCode;

public class AlertException extends BaseException {

    public AlertException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AlertException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

}
