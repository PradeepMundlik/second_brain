package com.secondbrain.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AiSummarizeResponse {
    @JsonProperty("note_id")
    private Long noteId;
    private String summary;
}
