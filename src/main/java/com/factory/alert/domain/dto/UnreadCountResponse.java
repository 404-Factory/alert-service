package com.factory.alert.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnreadCountResponse {

    private long totalCount;
    private long warningCount;
    private long criticalCount;
}