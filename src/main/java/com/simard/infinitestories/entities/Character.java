package com.simard.infinitestories.entities;

import com.simard.infinitestories.enums.CharacterTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "character_entity")
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "characterType", nullable = false)
    private String characterType;

    @Column(name = "description")
    private String description;


    public Character(Game game,String name, String description, CharacterTypeEnum characterType) {
        this.game = game;
        this.name = name;
        this.description = description;
        this.characterType = characterType.name();
    }

    public Character() {
        super();
    }
}
