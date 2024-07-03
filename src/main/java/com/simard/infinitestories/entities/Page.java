package com.simard.infinitestories.entities;

import com.simard.infinitestories.enums.CharacterTypeEnum;
import com.simard.infinitestories.enums.PageTypeEnum;
import com.theokanning.openai.completion.chat.ChatMessage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name="user_message", nullable = false, length = 1000)
    private String userMessage;

    @Column(name="completion", nullable = false, length = 5000)
    private String completion;

    @Column(name = "page_index")
    private int pageIndex = 0;

    @Column(name="page_type")
    @Enumerated(EnumType.STRING)
    private PageTypeEnum pageType;

    @OneToMany
    @JoinColumn(name = "page_id")
    private List<Character> characters;

    public Page(String userMessage, String completion) {
        this(userMessage, completion, PageTypeEnum.NORMAL, new ArrayList<>());
    }

    public Page(String userMessage, String completion, PageTypeEnum pageType, List<Character> characters) {
        this.userMessage = userMessage;
        this.completion = completion;
        this.pageType = pageType;
        this.characters = characters;
    }
}

