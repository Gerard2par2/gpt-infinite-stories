package com.simard.infinitestories.entities;

import com.simard.infinitestories.enums.CharacterTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "character_entity")
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "character_name", nullable = false)
    private String name;

    @Column(name = "character_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CharacterTypeEnum characterType;

    @Column(name = "character_description")
    private String description;

    @OneToMany
    @JoinColumn(name = "character_id")
    private List<CharacterSkillValue> skillValues;

    public Character(String name, String description, CharacterTypeEnum characterType) {
        this.name = name;
        this.description = description;
        this.characterType = characterType;
    }
}
