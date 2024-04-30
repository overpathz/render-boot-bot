package com.example.rendertestbot.notes.request;

import java.io.Serializable;

public record NoteDto(Long noteId, String noteText, Long userId) implements Serializable {
}
