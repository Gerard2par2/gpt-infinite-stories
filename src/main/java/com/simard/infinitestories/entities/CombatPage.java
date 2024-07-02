package com.simard.infinitestories.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class CombatPage extends Page {
    private List<Character> enemies;

    public CombatPage(String userMessage, String completion) {
        super(userMessage, completion);
        this.enemies = new ArrayList<>();
    }

    public CombatPage(String userMessage, String completion, List<Character> enemies) {
        super(userMessage, completion);
        this.enemies = new ArrayList<>(enemies);
    }
}
