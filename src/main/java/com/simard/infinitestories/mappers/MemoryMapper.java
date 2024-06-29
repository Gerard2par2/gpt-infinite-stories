package com.simard.infinitestories.mappers;

import com.simard.infinitestories.entities.Memory;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MemoryMapper {
    public List<ChatMessage> mapMemoryListToChatMessageList(List<Memory> memories) {
        List<ChatMessage> messages = new ArrayList<>();
        for(Memory memory : memories) {
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), memory.getDescription()));
        }
        return messages;
    }
}
