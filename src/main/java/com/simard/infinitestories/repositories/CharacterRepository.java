package com.simard.infinitestories.repositories;

import com.simard.infinitestories.entities.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    Character findCharacterByCompletionId(String completionId);
    @Query("SELECT DISTINCT c FROM Character c JOIN Page p ON c MEMBER OF p.characters JOIN Game g ON p MEMBER OF g.pages WHERE g.id = :gameId")
    List<Character> findAllByGameId(Long gameId);
}
