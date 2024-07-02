package com.simard.infinitestories.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum ActionResultsEnum {
    COMPLETE_FAILURE("COMPLETE_FAILURE"),
    CRITICAL_FAILURE("CRITICAL_FAILURE"),
    FAILURE("FAILURE"),
    SUCCESS("SUCCESS"),
    CRITICAL_SUCCESS("CRITICAL_SUCCESS"),
    COMPLETE_SUCCESS("COMPLETE_SUCCESS");

    private final String result;
}