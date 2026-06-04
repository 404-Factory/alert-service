package com.factory.alert.dto.request;

import com.factory.alert.validation.constraints.ValidSeverity;
import com.factory.alert.validation.constraints.ValidStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AlertUpdateRequest {

    @ValidStatus
    private String status;
    @ValidSeverity
    private String severity;
}
