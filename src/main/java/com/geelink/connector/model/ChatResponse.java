package com.geelink.connector.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatResponse {
    private List<Choice> choices;

    @Getter
    @Setter
    public static class Choice {

        private int index;
        private Message message;

        // constructors, getters and setters
    }

}
