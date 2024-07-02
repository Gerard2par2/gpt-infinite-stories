package com.simard.infinitestories.entities;

import com.simard.infinitestories.enums.WorldEraEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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
    @Enumerated(EnumType.STRING)
    private WorldEraEnum era;

    public World(String name, String description, WorldEraEnum era) {
        this.name = name;
        this.description = description;
        this.era = era;
    }

    protected World() {

    }
}
