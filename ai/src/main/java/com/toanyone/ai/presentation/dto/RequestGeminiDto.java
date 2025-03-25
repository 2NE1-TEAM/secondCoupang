package com.toanyone.ai.presentation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestGeminiDto {
    private List<Content> contents;

    public RequestGeminiDto(List<Content> contents) {
        this.contents = contents;
    }

    @Getter
    @Setter
    public static class Content {
        private List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }

    }

    @Getter
    @Setter
    public static class Part {
        private String text;

        public Part(String text) {
            this.text = text;
        }

    }
}
