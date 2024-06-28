package com.simard.infinitestories.services;

import com.simard.infinitestories.entities.World;
import com.simard.infinitestories.models.dto.WorldDto;
import com.simard.infinitestories.repositories.WorldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorldService {

    public final WorldRepository worldRepository;

    @Autowired
    public WorldService(WorldRepository worldRepository) {
        this.worldRepository = worldRepository;
    }

    public List<World> findAll() {
        return this.worldRepository.findAll();
    }

    public World findById(Long worldId) {
        return this.worldRepository.findById(worldId).orElseThrow();
    }

    public World createAndSaveNewWorld(String name, String description, String era) {
        return this.worldRepository.save(new World(name, description, era));
    }

    public World updateWorld(WorldDto worldDto, Long id) {
        return this.worldRepository.updateWorldById(id, worldDto.name(), worldDto.description(), worldDto.era());
    }

    public void deleteById(Long id) {
        this.worldRepository.deleteById(id);
    }
}
