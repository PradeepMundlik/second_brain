package com.secondbrain.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

// Internal DTO — sent to the Python AI service, not exposed in the public API
@Data
@Builder
public class AiSearchRequest {

    private String query;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("top_k")
    private int topK;
}
