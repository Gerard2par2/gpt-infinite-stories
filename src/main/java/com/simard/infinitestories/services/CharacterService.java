package com.simard.infinitestories.services;

import com.simard.infinitestories.enums.CharacterStatusEnum;
import com.simard.infinitestories.enums.CharacterTypeEnum;
import com.simard.infinitestories.repositories.CharacterRepository;
import org.springframework.stereotype.Service;

import com.simard.infinitestories.entities.Character;

import java.util.List;

@Service
public class CharacterService {
    private final CharacterRepository characterRepository;

    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }
    public Character createAndSaveNewCharacter(String completionId, String name, String description, CharacterTypeEnum characterType, CharacterStatusEnum status, String imagePrompt) {
        return this.characterRepository.save(new Character(completionId, name, description, characterType, status, imagePrompt));
    }

    public Character saveOrUpdateCharacterIfNeeded(Character generatedCharacter) {
        Character found = this.characterRepository.findCharacterByCompletionId(generatedCharacter.getCompletionId());
        if(found == null) {
            // Save the new character
            return this.characterRepository.save(generatedCharacter);
        }
        if(this.equalsExceptId(found, generatedCharacter)) {
            // Update the existing character
            found.setName(generatedCharacter.getName());
            found.setDescription(generatedCharacter.getDescription());
            found.setCharacterType(generatedCharacter.getCharacterType());
            found.setStatus(generatedCharacter.getStatus());
            found.setImagePrompt(generatedCharacter.getImagePrompt());
            return this.characterRepository.save(found);
        }
        return found;
    }

    private boolean equalsExceptId(Character char1, Character char2) {
        return char1.getName().equals(char2.getName())
                && char1.getDescription().equals(char2.getDescription())
                && char1.getCharacterType().equals(char2.getCharacterType())
                && char1.getStatus().equals(char2.getStatus())
                && char1.getImagePrompt().equals(char2.getImagePrompt());
    }

    public List<Character> findAllByGameId(Long gameId) {
        return this.characterRepository.findAllByGameId(gameId);
    }
}
