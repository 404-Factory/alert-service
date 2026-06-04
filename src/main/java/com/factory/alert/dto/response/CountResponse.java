package com.factory.alert.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CountResponse {

    private Long totalCount;
    private Long warningCount;
    private Long criticalCount;
}