package com.simard.infinitestories.mappers;


import com.simard.infinitestories.entities.Game;
import com.simard.infinitestories.models.dto.GameDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameMapper {
    public GameDto fromGameToGameDto(Game game) {
        return new GameDto(
                game.getId(),
                game.getCharacter().getDescription(),
                game.getCharacter().getName(),
                game.getGptModel(),
                game.getWorld().getId()
        );
    }

    public List<GameDto> fromGameToGameDtoList(List<Game> games) {
        return games.stream().map(this::fromGameToGameDto).toList();
    }
}
