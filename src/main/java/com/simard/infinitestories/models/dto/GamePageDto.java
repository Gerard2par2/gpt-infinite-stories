package com.simard.infinitestories.models.dto;

import com.simard.infinitestories.enums.PageTypeEnum;

public record GamePageDto (
        String userMessage,
        String completion,
        PageTypeEnum type
){}
