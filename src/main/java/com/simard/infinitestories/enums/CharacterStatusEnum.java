package com.simard.infinitestories.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CharacterStatusEnum {
    ALIVE("ALIVE"),
    DEAD("DEAD");
    private final String status;
}
