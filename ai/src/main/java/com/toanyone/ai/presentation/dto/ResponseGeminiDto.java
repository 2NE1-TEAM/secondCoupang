package com.toanyone.ai.presentation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseGeminiDto {
    private List<Candidate> candidates;

    @Getter
    @Setter
    public static class Candidate {
        private Content content;

    }

    @Getter
    @Setter
    public static class Content {
        private List<Part> parts;

    }

    @Getter
    @Setter
    public static class Part {
        private String text;

    }
}
