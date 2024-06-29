package com.simard.infinitestories.services;

import com.simard.infinitestories.entities.Game;
import com.simard.infinitestories.entities.World;
import com.simard.infinitestories.enums.CharacterTypeEnum;
import com.simard.infinitestories.repositories.CharacterRepository;
import org.springframework.stereotype.Service;

import com.simard.infinitestories.entities.Character;

import java.util.Optional;

@Service
public class CharacterService {
    private final CharacterRepository characterRepository;

    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }
    public Character createAndSaveNewCharacter(String name, String description, CharacterTypeEnum characterType) {
        return this.characterRepository.save(new Character(name, description, characterType));
    }
}
