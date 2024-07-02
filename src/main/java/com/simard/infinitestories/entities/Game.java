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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "player_character")
    private Character playerCharacter;

    @OneToMany
    @JoinColumn(name = "game_id")
    @OrderColumn(name = "page_index")
    private List<Page> pages;

    public Game(World world, String model, User user) {
        this.world = world;
        this.gptModel = model;
        this.user = user;
    }

    public Game() {
    }
}

