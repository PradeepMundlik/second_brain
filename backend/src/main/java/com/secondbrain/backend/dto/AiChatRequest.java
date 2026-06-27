package com.secondbrain.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiChatRequest {
    private String query;
    private String context;
    private List<ChatMessageDto> history;
}
