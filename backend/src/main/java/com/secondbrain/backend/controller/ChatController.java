package com.secondbrain.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondbrain.backend.dto.AiChatRequest;
import com.secondbrain.backend.dto.ChatRequest;
import com.secondbrain.backend.dto.SearchResult;
import com.secondbrain.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @PostMapping("/api/chat")
    public SseEmitter chat(@RequestBody ChatRequest req) {
        SseEmitter emitter = new SseEmitter(60_000L);

        emitter.onTimeout(emitter::complete);

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Retrieve relevant chunks + source card metadata
                ChatService.ChatContextResult ctx = chatService.buildContext(req.getQuery());

                // 2. Send source cards immediately so Angular can render them while LLM streams
                List<SearchResult> sources = ctx.sources();
                emitter.send(SseEmitter.event()
                        .name("sources")
                        .data(objectMapper.writeValueAsString(sources)));

                // 3. Stream LLM answer token by token
                AiChatRequest aiChatReq = AiChatRequest.builder()
                        .query(req.getQuery())
                        .context(ctx.context())
                        .history(req.getHistory())
                        .build();

                chatService.streamFromPython(aiChatReq, emitter);
                emitter.complete();

            } catch (Exception e) {
                log.error("Chat stream error: {}", e.getMessage(), e);
                try { emitter.completeWithError(e); } catch (Exception ignored) {}
            }
        });

        return emitter;
    }
}
