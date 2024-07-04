package com.simard.infinitestories.models.dto;

import com.simard.infinitestories.entities.Character;
import com.simard.infinitestories.enums.PageTypeEnum;

import java.util.List;

public record GamePageDto (
        String userMessage,
        String completion,
        PageTypeEnum type,
        List<Character> characters
){}
