package com.simard.infinitestories.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemoryTypeEnum {
    INFORMATION("INFORMATION"),
    LOCATION("LOCATION"),
    CHARACTER("CHARACTER"),
    ITEM("ITEM"),
    EVENT("EVENT");

    private final String type;
}
