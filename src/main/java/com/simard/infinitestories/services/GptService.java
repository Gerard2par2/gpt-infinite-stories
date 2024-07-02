package com.simard.infinitestories.services;

import com.simard.infinitestories.exceptions.InvalidCompletionException;
import com.simard.infinitestories.models.dto.ColorDto;
import com.simard.infinitestories.rest.WorldController;
import com.simard.infinitestories.utils.Prompts;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GptService {

    private OpenAiService _openAiService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("${com.simard.openAiToken}")
    private String openAiToken;

    private OpenAiService openAiService() {
        if(this._openAiService == null ){
            this._openAiService = new OpenAiService(this.openAiToken, Duration.ZERO);
        }
        return this._openAiService;
    }

    public String getCompletion(List<ChatMessage> messages) {
        return this.getCompletion(messages, "gpt-3.5-turbo");
    }

    public String getCompletion(List<ChatMessage> messages, String model) {
        this.logger.info("Getting completion from OpenAI with model {} and messages list {}", model, messages);
        ChatCompletionRequest req = new ChatCompletionRequest();
        req.setModel(model);
        req.setMessages(messages);
        ChatCompletionResult result = this.openAiService().createChatCompletion(req);
        return result.getChoices().get(result.getChoices().toArray().length - 1).getMessage().getContent();
    }

    public Map<String, ColorDto> getColorsFromCompletion(String completion) throws InvalidCompletionException {
        completion = completion.replace("\n", "");

        Map<String, ColorDto> colorsMap = new HashMap<>();
        String title;
//        String regex = "\\d{1,3},\\d{1,3},\\d{1,3}";

        String[] lines = completion.split(";");

        for (String colorString : lines) {
            title = colorString.substring(0, colorString.indexOf(":") + 1).replace(":", "").replace(" ", "");
            colorString = colorString.replace(title, "").replace(":", "").replace(" ", "");
            String[] splittedColorString = colorString.split(",");

            int redValue = Integer.parseInt(splittedColorString[0]);
            int greenValue = Integer.parseInt(splittedColorString[1]);
            int blueValue = Integer.parseInt(splittedColorString[2]);

            colorsMap.put(title, new ColorDto(redValue, greenValue, blueValue));
        }

        return colorsMap;
    }

    public List<ChatMessage> getStartMessages(String worldDescription, String playerCharacterDescription) {
        List<ChatMessage> messages = new ArrayList<>();

        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), Prompts.START_PROMPT));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), Prompts.userGameCreationDescriptionsMessage(playerCharacterDescription, worldDescription)));

        this.logger.info("Built start message list: {}", messages);
        
        return messages;
    }

}
