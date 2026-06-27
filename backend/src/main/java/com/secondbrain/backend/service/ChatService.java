package com.secondbrain.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.secondbrain.backend.client.AiServiceClient;
import com.secondbrain.backend.dto.AiChatRequest;
import com.secondbrain.backend.dto.AiSearchRequest;
import com.secondbrain.backend.dto.AiSearchResponse;
import com.secondbrain.backend.dto.SearchResult;
import com.secondbrain.backend.entity.Note;
import com.secondbrain.backend.repository.NoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final int TOP_K = 5;

    private final AiServiceClient aiServiceClient;
    private final NoteRepository noteRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate streamingRestTemplate;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public ChatService(AiServiceClient aiServiceClient, NoteRepository noteRepository) {
        this.aiServiceClient = aiServiceClient;
        this.noteRepository = noteRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(60_000);
        this.streamingRestTemplate = new RestTemplate(factory);
    }

    public record ChatContextResult(List<SearchResult> sources, String context) {}

    @Transactional(readOnly = true)
    public ChatContextResult buildContext(String query) {
        AiSearchResponse aiResponse = aiServiceClient.search(
                AiSearchRequest.builder()
                        .query(query)
                        .userId(DEFAULT_USER_ID)
                        .topK(TOP_K)
                        .build()
        );

        List<AiSearchResponse.Hit> hits = aiResponse.getResults();

        // Format context from chunk text only — titles are NOT included in LLM context
        String context = formatContext(hits);

        // Fetch titles from PostgreSQL only for the UI source cards
        List<Long> noteIds = hits.stream()
                .map(AiSearchResponse.Hit::getNoteId)
                .distinct()
                .toList();

        Map<Long, Note> notesById = noteRepository.findAllById(noteIds).stream()
                .collect(Collectors.toMap(Note::getId, Function.identity()));

        List<SearchResult> sources = hits.stream()
                .filter(h -> notesById.containsKey(h.getNoteId()))
                .map(h -> {
                    Note note = notesById.get(h.getNoteId());
                    return SearchResult.builder()
                            .noteId(note.getId())
                            .title(note.getTitle())
                            .excerpt(h.getExcerpt())
                            .score(h.getScore())
                            .tags(new java.util.ArrayList<>(note.getTags()))
                            .build();
                })
                .toList();

        return new ChatContextResult(sources, context);
    }

    private String formatContext(List<AiSearchResponse.Hit> hits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            sb.append("Excerpt ").append(i + 1).append(":\n");
            sb.append(hits.get(i).getExcerpt()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public void streamFromPython(AiChatRequest chatReq, SseEmitter emitter) throws JsonProcessingException {
        String body = objectMapper.writeValueAsString(chatReq);

        streamingRestTemplate.execute(
                URI.create(aiServiceUrl + "/chat"),
                HttpMethod.POST,
                request -> {
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    request.getBody().write(body.getBytes(StandardCharsets.UTF_8));
                },
                response -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) break;
                                try {
                                    emitter.send(SseEmitter.event().name("chunk").data(data));
                                } catch (IOException e) {
                                    log.debug("Client disconnected during chat stream");
                                    break;
                                }
                            }
                        }
                    }
                    return null;
                }
        );
    }
}
