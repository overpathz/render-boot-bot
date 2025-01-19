package com.example.rendertestbot.websocket;

import java.io.Serializable;

public record NoteDto(Long noteId, String noteText, Long userId) implements Serializable {
}
