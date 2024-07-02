package com.simard.infinitestories.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorldEraEnum {
    PALEOLITHIC("PALEOLITHIC"),
    NEOLITHIC("NEOLITHIC"),
    ANTIQUITY("ANTIQUITY"),
    MIDDLE_AGE("MIDDLE_AGE"),
    RENAISSANCE("RENAISSANCE"),
    MODERN("MODERN"),
    CONTEMPORARY("CONTEMPORARY"),
    NEAR_FUTURE("NEAR_FUTURE"),
    FAR_FUTURE("FAR_FUTURE"),
    ALIEN("ALIEN");

    private final String era;
}
