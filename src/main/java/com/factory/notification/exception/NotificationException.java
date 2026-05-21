package com.factory.notification.exception;

import com.factory.common.core.exception.BaseException;
import com.factory.common.core.exception.ErrorCode;

public class NotificationException extends BaseException {

    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

}
