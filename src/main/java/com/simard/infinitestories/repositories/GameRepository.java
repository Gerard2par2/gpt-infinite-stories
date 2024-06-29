package com.simard.infinitestories.repositories;

import com.simard.infinitestories.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    @Query("SELECT g FROM Game g WHERE g.world.id = ?1")
    List<Game> findAllByWorldId(Long worldId);
}
