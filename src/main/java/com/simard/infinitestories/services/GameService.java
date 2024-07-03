package com.simard.infinitestories.services;

import com.simard.infinitestories.entities.*;
import com.simard.infinitestories.entities.Character;
import com.simard.infinitestories.enums.ActionResultsEnum;
import com.simard.infinitestories.enums.CharacterTypeEnum;
import com.simard.infinitestories.enums.PageTypeEnum;
import com.simard.infinitestories.exceptions.InvalidCompletionException;
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
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Service for the game
 */
@Service
public class GameService {
    private final static int MAX_SAVED_PAGE_COUNT = 100;

    // Repositories
    private final GameRepository gameRepository;
    private final PageRepository pageRepository;

    // Services
    private final GptService gptService;
    private final WorldService worldService;
    private final UserService userService;
    private final CharacterService characterService;

    // Utils
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JsonParser jsonParser;

    private final Map<Long, Integer> completionRetryMap = new HashMap<>();

    private final static int MAX_COMPLETION_RETRIES = 3;

    @Autowired
    public GameService(
            GameRepository gameRepository,
            PageRepository pageRepository,
            GptService gptService,
            WorldService worldService,
            UserService userService,
            CharacterService characterService)
    {
        // Repositories
        this.gameRepository = gameRepository;
        this.pageRepository = pageRepository;

        // Services
        this.gptService = gptService;
        this.worldService = worldService;
        this.userService = userService;
        this.characterService = characterService;

        // Utils
        this.jsonParser = JsonParserFactory.getJsonParser();
    }

    public List<Game> findAllByWorldId(Long worldId) {
        return this.gameRepository.findAllByWorldId(worldId);
    }

    /**
     * Get the color palette for a world
     * @param gptModel The GPT model
     * @return The color palette
     */
    public Map<String, ColorDto> getColorPaletteForWorld(String gptModel) {
        Map<String, ColorDto> colors;
        List<ChatMessage> messages = new ArrayList<>();

        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), Prompts.COLORS_PROMPT));

        String completion = this.gptService.getCompletion(messages, gptModel);

        colors = this.gptService.getColorsFromCompletion(completion);

        return colors;
    }

    /**
     * Roll a die
     * @param characterSkillLevel The character skill level
     * @param rollOffset The roll offset
     * @return The action result
     */
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

    /**
     * Create a new game
     * @param gameCreationDto The game creation dto
     * @return The game creation response dto
     */
    public GameCreationResponseDto createNewGame(GameCreationDto gameCreationDto) {

        User user = this.userService.findById(gameCreationDto.userId());

        // Testing purposes, will be removed
        if(user == null) {
            user = this.userService.createAndSaveNewUser("user", "pwd");
        }

        World world = this.worldService.findById(gameCreationDto.worldId());

        if(world == null) {
            throw new MyApiException("World not found", HttpStatus.NOT_FOUND);
        }

        Game newGame = new Game(world, gameCreationDto.model(), user);

        Character playerCharacter = this.characterService.createAndSaveNewCharacter(
                gameCreationDto.playerCharacterName(),
                gameCreationDto.playerCharacterDescription(),
                CharacterTypeEnum.PLAYER
        );

        newGame.setPlayerCharacter(playerCharacter);

        newGame = this.gameRepository.save(newGame);

        return new GameCreationResponseDto(this.getColorPaletteForWorld(newGame.getGptModel()), newGame.getId());
    }

    /**
     * Get the next page of the game
     * @param gameId The game id
     * @param playerMessage The player message
     * @return The next page
     */
    public GamePageDto nextPage(@NotNull Long gameId, String playerMessage) {
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

        // Create and complete a new page
        Page page = new Page();
        this.completePage(game, page, messages, playerMessage);
        // Update the game with the new page
        game.getPages().add(page);
        // Limit saved pages count to MAX_SAVED_PAGE_COUNT
        if(game.getPages().size() > MAX_SAVED_PAGE_COUNT) {
            this.logger.info("Deleting oldest page of game {}", game.getId());
            Page deleted = game.getPages().remove(0);
            this.pageRepository.delete(deleted);
            for(Page currPage: game.getPages()) {
                this.logger.info("Current page index: {}", currPage.getPageIndex());
                if(currPage.getPageIndex() < deleted.getPageIndex()) {
                    throw new RuntimeException("Did not delete the oldest page");
                }
            }
        } else {
            // Save the new page
            this.pageRepository.save(page);
            // Save the updated game
            this.gameRepository.save(game);
        }

        return new GamePageDto(page.getUserMessage(), page.getCompletion(), page.getPageType());
    }

    /**
     * Completes the page with the given messages
     * Will retry the completion if the completion is invalid
     * @param game The current game
     * @param page The page to complete, will be mutated
     * @param messages The previous messages
     * @param playerMessage The player message
     */
    private void completePage(Game game, Page page, List<ChatMessage> messages, String playerMessage) {
        if(this.completionRetryMap.get(game.getId()) != null) this.logger.debug("LOOK MOM I'M RECURSIVE! (retrying completion)");
        // Get the completion
        String completion = this.gptService.getCompletion(messages, game.getGptModel());

        try {
            // Parse the completion
            @SuppressWarnings({"unchecked", "rawtypes"}) // Me not likey orange squiggles
            Map<String, String> parsedCompletion = (Map) this.jsonParser.parseMap(completion);
            // Set the page type and completion
            page.setPageType(PageTypeEnum.valueOf(parsedCompletion.get("type")));
            page.setCompletion(parsedCompletion.get("completion"));
            page.setUserMessage(playerMessage);
            logger.info("Parsed completion: {}", page);
        } catch (Exception e) {
            this.logger.warn("An error occured when completing page: {}", e.getMessage());
            if(!(e instanceof IllegalArgumentException)){
                throw new MyApiException("An error occurred when completing page: ".concat(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            // Get the retry counter for this game, set to 0 if not already in the map
            int retryCount = this.completionRetryMap.computeIfAbsent(game.getId(), k -> 0);
            if(retryCount < MAX_COMPLETION_RETRIES) {
                this.logger.warn("Invalid completion, retrying {}/3", retryCount + 1);
                // Increment the retry counter
                this.completionRetryMap.put(game.getId(), this.completionRetryMap.get(game.getId()) + 1);
                // Notify the llm that the completion was invalid
                messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "ERROR: ".concat(e.getMessage()).concat(" Please retry.")));
                // Remind the last player message
                messages.add(new ChatMessage(ChatMessageRole.USER.value(), playerMessage));
                // Retry the completion
                this.completePage(game, page, messages, playerMessage);
            } else {
                // Reset the retry counter
                this.completionRetryMap.remove(game.getId());
                throw new InvalidCompletionException("Invalid completion from OpenAI", completion);
            }
        }
        // Reset the retry counter
        this.completionRetryMap.remove(game.getId());
    }
}
