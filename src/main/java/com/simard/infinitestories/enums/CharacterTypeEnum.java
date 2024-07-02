package com.simard.infinitestories.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CharacterTypeEnum {
    PLAYER("PLAYER"),
    ALLY("ALLY"),
    NEUTRAL("NEUTRAL"),
    ENEMY("ENEMY");
    private final String type;
}
