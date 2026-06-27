package com.secondbrain.backend.service;

import com.secondbrain.backend.dto.NoteRequest;
import com.secondbrain.backend.dto.NoteResponse;
import com.secondbrain.backend.entity.Note;
import com.secondbrain.backend.exception.NoteNotFoundException;
import com.secondbrain.backend.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {

    // Temporary: hardcoded until JWT auth is added in Week 3
    private static final Long DEFAULT_USER_ID = 1L;

    private final NoteRepository noteRepository;
    private final EmbeddingService embeddingService;
    private final SummaryService summaryService;

    @Transactional
    public NoteResponse createNote(NoteRequest request) {
        Note note = Note.builder()
                .userId(DEFAULT_USER_ID)
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags())
                .build();

        Note saved = noteRepository.save(note);
        triggerEmbedding(saved);
        summaryService.summarizeAndSave(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getNotes() {
        return noteRepository.findByUserIdOrderByCreatedAtDesc(DEFAULT_USER_ID)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public NoteResponse updateNote(Long id, NoteRequest request) {
        Note note = findOrThrow(id);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setTags(request.getTags());

        Note saved = noteRepository.save(note);

        // Re-embed: delete old vectors first, then embed updated content
        try {
            embeddingService.deleteEmbeddings(id);
            embeddingService.embedNote(saved);
        } catch (Exception e) {
            log.error("Re-embedding failed for note {}: {}", id, e.getMessage());
        }
        summaryService.summarizeAndSave(saved);

        return toResponse(saved);
    }

    @Transactional
    public void deleteNote(Long id) {
        Note note = findOrThrow(id);
        try {
            embeddingService.deleteEmbeddings(id);
        } catch (Exception e) {
            log.error("Embedding deletion failed for note {}: {}", id, e.getMessage());
        }
        noteRepository.delete(note);
    }

    private void triggerEmbedding(Note note) {
        try {
            embeddingService.embedNote(note);
        } catch (Exception e) {
            // Embedding failure must not fail note creation — note is already saved
            log.error("Embedding failed for note {}: {}", note.getId(), e.getMessage());
        }
    }

    private Note findOrThrow(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .summary(note.getSummary())
                .tags(note.getTags())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
