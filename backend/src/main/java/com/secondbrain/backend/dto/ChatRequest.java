package com.secondbrain.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private String query;
    private List<ChatMessageDto> history;
}
