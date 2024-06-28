package com.simard.infinitestories.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name= "`name`", length = 50, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @Column(name = "gpt_model", nullable = false)
    private String gptModel;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @OneToOne
    private Character character;

    public Game(World world, String model, Player player) {
        this.world = world;
        this.gptModel = model;
        this.player = player;
    }

    public Game() {

    }
}
