package com.example.rendertestbot.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> getNoteByUniqueLinkId(String uniqueLinkId);
}
