package com.secondbrain.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchRequest {

    @NotBlank(message = "Query cannot be blank")
    private String query;
}
