package com.simard.infinitestories.services;

import com.simard.infinitestories.entities.*;
import com.simard.infinitestories.entities.Character;
import com.simard.infinitestories.enums.ActionResultsEnum;
import com.simard.infinitestories.enums.CharacterTypeEnum;
import com.simard.infinitestories.exceptions.MyApiException;
import com.simard.infinitestories.models.dto.ColorDto;
import com.simard.infinitestories.models.dto.GameCreationDto;
import com.simard.infinitestories.models.dto.GameCreationResponseDto;
import com.simard.infinitestories.models.dto.GamePageDto;
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
    private final static int MAX_SAVED_PAGE_COUNT = 100;

    private final GameRepository gameRepository;
    private final PageRepository pageRepository;

    // Services
    private final GptService gptService;
    private final WorldService worldService;
    private final UserService userService;
    private final CharacterService characterService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public GameService(
            GameRepository gameRepository,
            PageRepository pageRepository,
            GptService gptService,
            WorldService worldService,
            UserService userService,
            CharacterService characterService)
    {
        this.gameRepository = gameRepository;
        this.pageRepository = pageRepository;
        this.gptService = gptService;
        this.worldService = worldService;
        this.userService = userService;
        this.characterService = characterService;
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

        Game newGame = new Game(world, gameCreationDto.model(), user);

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
        Game game = this.gameRepository.findById(gameId).orElseThrow(() -> new MyApiException("Game not found", HttpStatus.NOT_FOUND));
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

        // Get the completion
        String completion = this.gptService.getCompletion(messages, game.getGptModel());

        Page page = new Page(playerMessage, completion);

        // Create and add the new page to the game
        game.getPages().add(page);

        // Save the new page
        this.pageRepository.save(page);
        // Save the updated game
        this.gameRepository.save(game);

        // Limit saved pages count to MAX_SAVED_PAGE_COUNT
        if(game.getPages().size() > MAX_SAVED_PAGE_COUNT) {
            this.logger.info("Deleting oldest page");
            Page deleted = game.getPages().remove(0);
            this.pageRepository.delete(deleted);
            for(Page currPage: game.getPages()) {
                this.logger.info("Current page index: {}", currPage.getPageIndex());
                if(currPage.getPageIndex() < deleted.getPageIndex()) {
                    throw new RuntimeException("Did not delete the oldest page");
                }
            }
        }

        return ResponseEntity.ok(new GamePageDto(playerMessage, completion));
    }
}
