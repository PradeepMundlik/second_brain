package com.secondbrain.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NoteRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title must be under 255 characters")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    private List<String> tags = new ArrayList<>();
}
