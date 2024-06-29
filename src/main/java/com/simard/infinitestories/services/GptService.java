package com.simard.infinitestories.services;

import com.simard.infinitestories.exceptions.InvalidCompletionException;
import com.simard.infinitestories.models.dto.ColorDto;
import com.simard.infinitestories.utils.Prompts;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class GptService {

    private final OpenAiService openAiService;

    @Autowired
    public GptService(Environment environment) {
        String token = environment.getProperty("com.simard.openAiToken");
        assert token != null;
        this.openAiService = new OpenAiService(token, Duration.ZERO);
    }

    public String getCompletion(ChatMessage message, String model) {return this.getCompletion(List.of(message), model);}
    public String getCompletion(ChatMessage message) {return this.getCompletion(List.of(message));}
    public String getCompletion(List<ChatMessage> messages) {
        return this.getCompletion(messages, "gpt-3.5-turbo");
    }

    public String getCompletion(List<ChatMessage> messages, String model) {
        ChatCompletionRequest req = new ChatCompletionRequest();
        req.setModel(model);
        req.setMessages(messages);
        return this.openAiService.createChatCompletion(req).getChoices().get(0).getMessage().getContent();
    }

    public Map<String, ColorDto> getColorsFromCompletion(String completion) throws InvalidCompletionException {
        List<String> expectedTitles = new ArrayList<>(List.of(new String[]{"BACKGROUND", "TEXT", "WEAK-ACCENT", "STRONG-ACCENT"}));

        completion = completion.replace("\n", "");

        Map<String, ColorDto> colorsMap = new HashMap<>();
        String title;
        String regex = "\\d{1,3},\\d{1,3},\\d{1,3}";

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

        return messages;
    }

}
