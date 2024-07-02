package com.simard.infinitestories.services;

import com.simard.infinitestories.entities.Player;
import com.simard.infinitestories.entities.User;
import com.simard.infinitestories.repositories.PlayerRepository;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
    public Player createAndSaveNewPlayer(User user) {
        return this.playerRepository.save(new Player(user));
    }
}
