package com.secondbrain.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResult {

    private Long noteId;
    private String title;
    private String excerpt;   // the specific chunk that matched — displayed as a snippet
    private Double score;     // cosine similarity 0–1 (higher = more relevant)
    private List<String> tags;
}
