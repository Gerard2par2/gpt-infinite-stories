package com.simard.infinitestories.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Page { // TODO: DELETE ???
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name="text", nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    public Page(World world, String text) {
        this.text = text;
        this.world = world;
    }

    public Page() {

    }
}
