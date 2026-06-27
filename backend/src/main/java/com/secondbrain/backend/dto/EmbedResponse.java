package com.secondbrain.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EmbedResponse {

    @JsonProperty("note_id")
    private Long noteId;

    private List<ChunkResult> chunks;

    @Data
    public static class ChunkResult {

        @JsonProperty("chunk_index")
        private Integer chunkIndex;

        @JsonProperty("chunk_text")
        private String chunkText;

        @JsonProperty("qdrant_point_id")
        private String qdrantPointId;
    }
}
