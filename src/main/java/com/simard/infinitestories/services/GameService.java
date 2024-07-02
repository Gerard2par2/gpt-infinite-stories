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
import com.simard.infinitestories.models.dto.*;
import com.simard.infinitestories.repositories.*;
import com.simard.infinitestories.utils.Prompts;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    public Map<String, ColorDto> getColorPaletteForWorld(String gptModel) {
        Map<String, ColorDto> colors;
        List<ChatMessage> messages = new ArrayList<>();

        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), Prompts.COLORS_PROMPT));

        String completion = this.gptService.getCompletion(messages, gptModel);

        colors = this.gptService.getColorsFromCompletion(completion);

        return colors;
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

        Game newGame = new Game(world, gameCreationDto.model(), player);

        Character playerCharacter = this.characterService.createAndSaveNewCharacter(
                gameCreationDto.playerCharacterName(),
                gameCreationDto.playerCharacterDescription(),
                CharacterTypeEnum.PLAYER
        );

        newGame.setPlayerCharacter(playerCharacter);

        newGame = this.gameRepository.save(newGame);

        return ResponseEntity.ok(new GameCreationResponseDto(this.getColorPaletteForWorld(newGame.getGptModel()), newGame.getId()));
    }

    public ResponseEntity<GamePageDto> nextPage(@NotNull Long gameId, String playerMessage) {
        // Get the game
        Game game = this.gameRepository.findById(gameId).orElseThrow(() -> new RequestException("Game not found", HttpStatus.NOT_FOUND));
        this.logger.info(game.getPages().toString());
        // Init a messages array with the start messages
        List<ChatMessage> messages = new ArrayList<>(
                this.gptService.getStartMessages(game.getWorld().getDescription(), game.getPlayerCharacter().getDescription())
        );

        // Add messages for each previous page
        ChatMessage previousUserMessage;
        ChatMessage previousCompletionMessage;
        for(Page page: game.getPages()) {
            previousUserMessage = new ChatMessage(ChatMessageRole.USER.value(), page.getUserMessage());
            previousCompletionMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), page.getCompletion());
            messages.add(previousUserMessage);
            messages.add(previousCompletionMessage);
        }

        // Add the new user message
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), playerMessage));

        // Add the saved memories
        messages.addAll(this.memoryMapper.mapMemoryListToChatMessageList(this.memoryService.getMemoriesByGameId(game.getId())));

        // Get the completion
        String completion = this.gptService.getCompletion(messages, game.getGptModel());

        // Extract memories from the completion
        if(completion.contains("MEMORY:") && !completion.endsWith("MEMORY:")) {
            completion = this.extractAndSaveMemorySection(completion, game);
        }

        // Create and add the new page to the game
        game.getPages().add(new Page(playerMessage, completion));
        // Save the updated game
        this.gameRepository.save(game);

        return ResponseEntity.ok(new GamePageDto(playerMessage, completion));
    }

    private String extractAndSaveMemorySection(String completion, Game game) {
        String[] completionSections = completion.split("MEMORY:");

        completion = completionSections[0];
        String memorySection = completionSections[1];

        this.logger.info("saving memory: {}", memorySection);
        this.memoryService.createAndSaveNewMemory(game, memorySection);

        return completion;
    }
}
