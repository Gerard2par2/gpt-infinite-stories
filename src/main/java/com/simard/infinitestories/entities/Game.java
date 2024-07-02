package com.simard.infinitestories.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @Column(name = "gpt_model", nullable = false)
    private String gptModel;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @OneToOne
    @JoinColumn(name = "payer_character")
    private Character playerCharacter;

    @OneToMany
    @JoinColumn(name = "game_id")
    private List<Page> pages;

    public Game(World world, String model, Player player) {
        this.world = world;
        this.gptModel = model;
        this.player = player;
    }

    public Game() {

    }
}
