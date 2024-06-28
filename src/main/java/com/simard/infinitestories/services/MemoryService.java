package com.simard.infinitestories.services;

import com.simard.infinitestories.entities.Game;
import com.simard.infinitestories.entities.Memory;
import com.simard.infinitestories.repositories.MemoryRepository;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryService {

    // Repositories
    private final MemoryRepository memoryRepository;

    @Autowired
    public MemoryService(MemoryRepository memoryRepository) {
        this.memoryRepository = memoryRepository;
    }
    public List<Memory> getMemoriesByGameId(Long gameId) {
        return this.memoryRepository.findAllByGameId(gameId).orElse(null);
    }
    public Memory createAndSaveNewMemory(Game game, String description) {
        return this.memoryRepository.save(new Memory(game, description));
    }
}
