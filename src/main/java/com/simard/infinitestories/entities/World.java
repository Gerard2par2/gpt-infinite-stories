package com.simard.infinitestories.entities;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Entity
@NoArgsConstructor
public class World {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "world_name", nullable = false)
    private String name;

    @Column(name="world_description", nullable = false)
    private String description;

    @Column(name="world_era", nullable = false)
    private String era;

    public World(String name, String description, String era) {
        this.name = name;
        this.description = description;
        this.era = era;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEra() {
        return era;
    }

    public void setEra(String era) {
        this.era = era;
    }
}
