package com.secondbrain.backend.service;

import com.secondbrain.backend.client.AiServiceClient;
import com.secondbrain.backend.dto.AiSummarizeRequest;
import com.secondbrain.backend.dto.AiSummarizeResponse;
import com.secondbrain.backend.entity.Note;
import com.secondbrain.backend.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private static final int SUMMARY_MIN_LENGTH = 500;

    private final AiServiceClient aiServiceClient;
    private final NoteRepository noteRepository;

    public void summarizeAndSave(Note note) {
        if (note.getContent().length() <= SUMMARY_MIN_LENGTH) {
            return;
        }
        try {
            AiSummarizeResponse response = aiServiceClient.summarize(
                    AiSummarizeRequest.builder()
                            .noteId(note.getId())
                            .title(note.getTitle())
                            .content(note.getContent())
                            .build()
            );
            note.setSummary(response.getSummary());
            noteRepository.save(note);
            log.info("Summary saved for note {}", note.getId());
        } catch (Exception e) {
            log.error("Summarization failed for note {}: {}", note.getId(), e.getMessage());
        }
    }
}
