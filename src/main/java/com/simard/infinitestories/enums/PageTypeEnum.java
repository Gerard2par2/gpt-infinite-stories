package com.simard.infinitestories.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PageTypeEnum {
    NORMAL("NORMAL"),
    COMBAT("COMBAT"),
    DIALOGUE("DIALOGUE");
    private final String type;
}
