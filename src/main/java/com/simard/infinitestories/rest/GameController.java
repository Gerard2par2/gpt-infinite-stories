package com.simard.infinitestories.rest;

import com.simard.infinitestories.mappers.GameMapper;
import com.simard.infinitestories.models.dto.GameCreationDto;
import com.simard.infinitestories.models.dto.GameCreationResponseDto;
import com.simard.infinitestories.models.dto.GameDto;
import com.simard.infinitestories.models.dto.GamePageDto;
import com.simard.infinitestories.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;
    private final GameMapper gameMapper;

    @Autowired
    public GameController(
            GameService gameService,
            GameMapper gameMapper) {
        this.gameService = gameService;
        this.gameMapper = gameMapper;
    }

    /**
     * Returns the map of the world
     * @return an HTTP response containing the map of the world
     */
    @GetMapping("/map")
    public ResponseEntity<String> getGameWorldMap() {
        return ResponseEntity.ok("Hello World");
    }

    /**
     * Returns all the games of a world
     * @param worldId the id of the world
     * @return an HTTP response containing all the games of the world
     */
    @GetMapping("/world/{worldId}")
    public ResponseEntity<List<GameDto>> getAllGamesByWorldId(@PathVariable Long worldId) {
        return ResponseEntity.ok(this.gameMapper.fromGameToGameDtoList(this.gameService.findAllByWorldId(worldId)));
    }

    /**
     * Creates a new game and generates a color palette for the application
     * @param gameCreationDto contains the user id, the game id,the gpt model to use,the description of the universe and the player character
     * @return an HTTP response containing a color palette fitting the universe to use in the frontend application and the game id
     */
    @PostMapping("/new")
    public ResponseEntity<GameCreationResponseDto> createNewGame(@RequestBody @Validated GameCreationDto gameCreationDto) {
        return this.gameService.createNewGame(gameCreationDto);
    }

    /**
     * Generates the next page of a game
     * @param gameId the id of the game
     * @return an HTTP response containing the next page of the appropriate game
     */
    @GetMapping("/{gameId}/start")
    public ResponseEntity<GamePageDto> startGame(@PathVariable Long gameId) {
        return this.gameService.startGame(gameId);
    }

    @PostMapping("/{gameId}/next")
    public ResponseEntity<GamePageDto> nextPage(@PathVariable Long gameId, @RequestBody @Validated String playerMessage) {
        return this.gameService.nextPage(gameId, playerMessage);
    }
}
