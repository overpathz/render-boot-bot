package com.example.rendertestbot.notes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> getNoteByUniqueLinkId(String uniqueLinkId);
    List<Note> getAllByUserId(Long userId);
}
