package com.secondbrain.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// Internal DTO — mirrors Python SearchResponse schema
@Data
public class AiSearchResponse {

    private List<Hit> results;

    @Data
    public static class Hit {

        @JsonProperty("note_id")
        private Long noteId;

        private String excerpt;
        private Double score;
    }
}
