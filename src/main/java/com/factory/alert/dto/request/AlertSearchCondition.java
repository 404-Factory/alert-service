package com.factory.alert.dto.request;

import com.factory.alert.validation.constraints.ValidSeverity;
import com.factory.alert.validation.constraints.ValidStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class AlertSearchCondition {

    @ValidStatus
    private String status;
    @ValidSeverity
    private String severity;
}
