package com.secondbrain.backend.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private String role;     // "user" or "assistant"
    private String content;
}
