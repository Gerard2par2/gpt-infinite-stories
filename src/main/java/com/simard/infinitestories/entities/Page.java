package com.simard.infinitestories.entities;

import com.theokanning.openai.completion.chat.ChatMessage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name="user_message", nullable = false, length = 1000)
    private String userMessage;

    @Column(name="completion", nullable = false, length = 1000)
    private String completion;

    public Page(String userMessage, String completion) {
        this.userMessage = userMessage;
        this.completion = completion;
    }
}
