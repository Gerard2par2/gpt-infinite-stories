package com.simard.infinitestories.rest;

import com.simard.infinitestories.entities.World;
import com.simard.infinitestories.models.dto.WorldCreationDto;
import com.simard.infinitestories.models.dto.WorldDto;
import com.simard.infinitestories.services.WorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/world")
public class WorldController {

    private final WorldService worldService;
    private final Logger logger = LoggerFactory.getLogger(WorldController.class);

    @Autowired
    public WorldController(
            WorldService worldService) {
        this.worldService = worldService;
    }

    @GetMapping("")
    public ResponseEntity<List<World>> getAllWorlds() {
        this.logger.debug("GET /world");
        return ResponseEntity.ok(this.worldService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<World> getWorldById(@PathVariable("id") Long id) {
        this.logger.debug("GET /world/{}", id);
        return ResponseEntity.ok(this.worldService.findById(id));
    }
    
    @PostMapping("/new")
    public ResponseEntity<World> createNewWorld(@RequestBody @Validated final WorldCreationDto worldCreationDto) {
        this.logger.debug("POST /world/new");
        return ResponseEntity.ok(this.worldService.createAndSaveNewWorld(worldCreationDto.name(), worldCreationDto.description(), worldCreationDto.era()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<World> updateWorld(@PathVariable("id") Long id, @RequestBody @Validated final WorldDto worldDto) {
        this.logger.debug("PUT /world/{}", id);
        return ResponseEntity.ok(this.worldService.updateWorld(worldDto, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorld(@PathVariable("id") Long id) {
        this.logger.debug("DELETE /world/{}", id);
        this.worldService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
