package com.simard.infinitestories.entities;

import com.simard.infinitestories.enums.MemoryTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Setter
@NoArgsConstructor
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "type")
    private String type;

    public Memory(Game game, String description) {
        this.game = game;
        this.description = description;
        this.type = MemoryTypeEnum.INFORMATION.name();
    }

    public Memory(Game game, String description, MemoryTypeEnum type) {
        this.game = game;
        this.description = description;
        this.type = type.name();
    }
}
