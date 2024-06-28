package com.simard.infinitestories.services;

import com.simard.infinitestories.entities.*;
import com.simard.infinitestories.entities.Character;
import com.simard.infinitestories.enums.ActionResultsEnum;
import com.simard.infinitestories.enums.CharacterTypeEnum;
import com.simard.infinitestories.exceptions.RequestException;
import com.simard.infinitestories.mappers.MemoryMapper;
import com.simard.infinitestories.models.dto.ColorDto;
import com.simard.infinitestories.models.dto.GameCreationDto;
import com.simard.infinitestories.models.dto.GameCreationResponseDto;
import com.simard.infinitestories.models.dto.GamePageDto;
import com.simard.infinitestories.repositories.*;
import com.simard.infinitestories.utils.Prompts;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class GameService {

    private final GameRepository gameRepository;

    // Services
    private final GptService gptService;
    private final MemoryService memoryService;
    private final WorldService worldService;
    private final UserService userService;
    private final PlayerService playerService;
    private final CharacterService characterService;

    // Mappers
    private final MemoryMapper memoryMapper;

    @Autowired
    public GameService(
            GameRepository gameRepository,
            GptService gptService,
            MemoryService memoryService,
            WorldService worldService,
            UserService userService,
            PlayerService playerService,
            CharacterService characterService,
            MemoryMapper memoryMapper)
    {
        this.gameRepository = gameRepository;

        this.gptService = gptService;
        this.memoryService = memoryService;
        this.worldService = worldService;
        this.userService = userService;
        this.playerService = playerService;
        this.characterService = characterService;

        this.memoryMapper = memoryMapper;
    }

    public List<Game> findAllByWorldId(Long worldId) {
        return this.gameRepository.findAllByWorldId(worldId);
    }

    public Game createGame (GameCreationDto gameCreationDto) {
        return null;
    }

    public Map<String, ColorDto> getColorPaletteForWorld() {
        Map<String, ColorDto> colors;
        List<ChatMessage> messages = new ArrayList<>();

        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), Prompts.COLORS_PROMPT));

        String completion = this.gptService.getCompletion(messages);

        colors = this.gptService.getColorsFromCompletion(completion);

        return colors;
    }

    public Game createAndSaveNewGame(World world, String model, Player player) {
        return this.gameRepository.save(new Game(world, model, player));
    }

    public ActionResultsEnum actionRoll (int characterSkillLevel, int rollOffset) {
        Random rd = new Random();

        // Get a random number between 1 and 20 with the bonus / malus added
        int rollResult = rd.nextInt(20) + 1 + rollOffset;

        // Get the minimum roll value for success
        int minForSuccess = 20 - characterSkillLevel;

        // Difference between the roll result and the min tu succeed
        int diff = minForSuccess - rollResult;

        if (diff <= 0) {
            //success
            if (diff >= -4) { return ActionResultsEnum.SUCCESS; }
            else if (diff >= -8) { return ActionResultsEnum.CRITICAL_SUCCESS; }
            else { return ActionResultsEnum.COMPLETE_SUCCESS; }
        } else {
            // Failure
            if (diff < 4) { return ActionResultsEnum.FAILURE; }
            else if (diff < 8) { return ActionResultsEnum.CRITICAL_FAILURE; }
            else { return ActionResultsEnum.COMPLETE_FAILURE; }
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GameCreationResponseDto> createNewGame(GameCreationDto gameCreationDto) {

        User user = this.userService.findById(gameCreationDto.userId());

        // Testing purposes, will be removed
        if(user == null) {
            user = this.userService.createAndSaveNewUser("user", "pwd");
        }

        World world = this.worldService.findById(gameCreationDto.worldId());

        if(world == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Player player = this.playerService.createAndSaveNewPlayer(user);

        Game newGame = this.createAndSaveNewGame(world, gameCreationDto.model(), player);

        Character character = this.characterService.createAndSaveNewCharacter(
                newGame,
                gameCreationDto.playerCharacterName(),
                gameCreationDto.playerCharacterDescription(),
                CharacterTypeEnum.PLAYER
        );

        newGame.setCharacter(character);

        return ResponseEntity.ok(new GameCreationResponseDto(this.getColorPaletteForWorld(), newGame.getId()));
    }

    public ResponseEntity<GamePageDto> startGame(@NotNull Long gameId) {
        Game game = this.gameRepository.findById(gameId).orElse(null);

        if(game == null) {
            throw new RequestException("Game not found", HttpStatus.NOT_FOUND);
        }

        String playerCharacterDescription = this.characterService.findPlayerByGameId(gameId).getDescription();
        
        return this.nextPage(game, this.gptService.getStartMessages(game.getWorld().getDescription(), playerCharacterDescription));
    }

    public ResponseEntity<GamePageDto> nextPage(@NotNull Long gameId, String playerMessage) {
        Game game = this.gameRepository.findById(gameId).orElseThrow();
        List<ChatMessage> messages = this.memoryMapper.mapMemoryListToChatMessageList(this.memoryService.getMemoriesByGameId(gameId));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), playerMessage));
        return nextPage(game, messages);
    }


    public ResponseEntity<GamePageDto> nextPage(@NotNull Game game, List<ChatMessage> messages) {
        Player player = playerService.getPlayerByGameId(game.getId());

        if(player == null) {
            throw new RequestException("Player not found", HttpStatus.NOT_FOUND);
        }

        String completion = this.gptService.getCompletion(messages);

        if(completion.contains("MEMORY:") && !completion.endsWith("MEMORY:")) {
            String[] completionSections = completion.split("MEMORY:");

            completion = completionSections[0];
            String memorySection = completionSections[1];

            System.out.println("saving memory: " + memorySection);
            this.memoryService.createAndSaveNewMemory(game, memorySection);
        }

        return ResponseEntity.ok(new GamePageDto(completion));
    }
}
