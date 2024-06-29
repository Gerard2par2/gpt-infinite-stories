package com.simard.infinitestories.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class CombatPage extends Page { // TODO: DELETE ???
    private List<Character> enemies;

    public CombatPage(World world, String text) {
        super(world, text);
    }

    public CombatPage(World world, String text, List<Character> enemies) {
        super(world, text);
        this.enemies = new ArrayList<>(enemies);
    }

}
