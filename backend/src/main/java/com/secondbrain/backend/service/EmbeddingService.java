package com.secondbrain.backend.service;

import com.secondbrain.backend.client.AiServiceClient;
import com.secondbrain.backend.dto.EmbedRequest;
import com.secondbrain.backend.dto.EmbedResponse;
import com.secondbrain.backend.entity.Note;
import com.secondbrain.backend.entity.NoteChunk;
import com.secondbrain.backend.repository.NoteChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private static final Long DEFAULT_USER_ID = 1L;

    private final AiServiceClient aiServiceClient;
    private final NoteChunkRepository noteChunkRepository;

    @Transactional
    public void embedNote(Note note) {
        EmbedRequest request = EmbedRequest.builder()
                .noteId(note.getId())
                .userId(DEFAULT_USER_ID)
                .title(note.getTitle())
                .content(note.getContent())
                .build();

        EmbedResponse response = aiServiceClient.embed(request);

        List<NoteChunk> chunks = response.getChunks().stream()
                .map(c -> NoteChunk.builder()
                        .noteId(note.getId())
                        .chunkIndex(c.getChunkIndex())
                        .chunkText(c.getChunkText())
                        .qdrantPointId(c.getQdrantPointId())
                        .build())
                .toList();

        noteChunkRepository.saveAll(chunks);
        log.info("Embedded note {} into {} chunks", note.getId(), chunks.size());
    }

    @Transactional
    public void deleteEmbeddings(Long noteId) {
        noteChunkRepository.deleteByNoteId(noteId);
        aiServiceClient.deleteEmbeddings(noteId);
        log.info("Deleted embeddings for note {}", noteId);
    }
}
