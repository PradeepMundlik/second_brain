package com.secondbrain.backend.service;

import com.secondbrain.backend.client.AiServiceClient;
import com.secondbrain.backend.dto.AiSearchRequest;
import com.secondbrain.backend.dto.AiSearchResponse;
import com.secondbrain.backend.dto.SearchResult;
import com.secondbrain.backend.entity.Note;
import com.secondbrain.backend.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final int TOP_K = 5;

    private final AiServiceClient aiServiceClient;
    private final NoteRepository noteRepository;

    @Transactional(readOnly = true)
    public List<SearchResult> search(String query) {
        AiSearchResponse aiResponse = aiServiceClient.search(
                AiSearchRequest.builder()
                        .query(query)
                        .userId(DEFAULT_USER_ID)
                        .topK(TOP_K)
                        .build()
        );

        if (aiResponse.getResults().isEmpty()) {
            return List.of();
        }

        // Qdrant gives us note_id + excerpt + score.
        // We fetch title and tags from PostgreSQL (single source of truth) in one query.
        List<Long> noteIds = aiResponse.getResults().stream()
                .map(AiSearchResponse.Hit::getNoteId)
                .distinct()
                .toList();

        Map<Long, Note> notesById = noteRepository.findAllById(noteIds).stream()
                .collect(Collectors.toMap(Note::getId, Function.identity()));

        // Preserve Qdrant's ranking order (highest cosine similarity first)
        return aiResponse.getResults().stream()
                .filter(hit -> notesById.containsKey(hit.getNoteId()))
                .map(hit -> {
                    Note note = notesById.get(hit.getNoteId());
                    return SearchResult.builder()
                            .noteId(note.getId())
                            .title(note.getTitle())
                            .excerpt(hit.getExcerpt())
                            .score(hit.getScore())
                            .tags(note.getTags())
                            .build();
                })
                .toList();
    }
}
