package com.simard.infinitestories.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CharacterSkillValue {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "skill_value", nullable = false)
    private Integer skillValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
