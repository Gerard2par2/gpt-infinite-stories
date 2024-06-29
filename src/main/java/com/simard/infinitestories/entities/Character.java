package com.simard.infinitestories.entities;

import com.simard.infinitestories.enums.CharacterTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Getter
@Setter
@Table(name = "character_entity")
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "characterType", nullable = false)
    @Enumerated(EnumType.STRING)
    private CharacterTypeEnum characterType;

    @Column(name = "description")
    private String description;

    public Character() {
        super();
    }

    public Character(String name, String description, CharacterTypeEnum characterType) {
        this.name = name;
        this.description = description;
        this.characterType = characterType;
    }
}
