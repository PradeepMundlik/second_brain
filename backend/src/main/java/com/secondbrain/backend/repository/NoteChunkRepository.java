package com.secondbrain.backend.repository;

import com.secondbrain.backend.entity.NoteChunk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteChunkRepository extends JpaRepository<NoteChunk, Long> {

    void deleteByNoteId(Long noteId);
}
