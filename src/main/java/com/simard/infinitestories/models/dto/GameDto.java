package com.simard.infinitestories.models.dto;

public record GameDto (
    Long id,
    String playerCharacterDescription,
    String playerCharacterName,

    String model,
    Long worldId) {
}
