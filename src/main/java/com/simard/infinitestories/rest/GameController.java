package com.simard.infinitestories.rest;

import com.simard.infinitestories.exceptions.UnkownUserException;
import com.simard.infinitestories.models.dto.GameCreationDto;
import com.simard.infinitestories.models.dto.GameCreationResponseDto;
import com.simard.infinitestories.models.dto.GamePageDto;
import com.simard.infinitestories.models.dto.NextPageRequestDto;
import com.simard.infinitestories.services.GameService;
import com.simard.infinitestories.services.GptService;
import jakarta.websocket.server.PathParam;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
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
     * Generates the first page of a game
     * @param gameId the id of the game
     * @return an HTTP response containing the next page
     */
    @GetMapping("/{gameId}/start")
    public ResponseEntity<GamePageDto> startGame(@PathVariable Long gameId) {
        return this.gameService.startGame(gameId);
    }

    /**
     * Generates the next page of a game
     * @param gameId
     * @return an HTTP response containing the next page
     */
    @PostMapping("/{gameId}/next")
    public ResponseEntity<GamePageDto> nextPage(@PathVariable Long gameId, @RequestBody @NotNull @Validated final NextPageRequestDto nextPageRequestDto) {
        return this.gameService.nextPage(gameId, nextPageRequestDto);
    }
}
