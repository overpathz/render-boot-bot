package com.example.rendertestbot.notes;

import com.example.rendertestbot.notes.request.BaseWsRequest;
import com.example.rendertestbot.notes.request.NoteDto;
import com.example.rendertestbot.notes.request.UpdateNote;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class NoteWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final RedisTemplate<String, String> redisTemplate;
    private final NoteRepository noteRepository;

    public NoteWebSocketHandler(ObjectMapper objectMapper, NoteRepository noteRepository, RedisTemplate<String, String> redisTemplate) {
        this.objectMapper = objectMapper;
        this.noteRepository = noteRepository;
        this.redisTemplate = redisTemplate;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!session.isOpen()) {
            return;
        }
        log.info("connection established");
        Long userId = getUserIdFromWsSession(session);
        redisTemplate.opsForValue().set("wsi_"+session.getId(), String.valueOf(userId));
    }

    private static Long getUserIdFromWsSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        return Long.valueOf(query.split("=")[1]);
    }

    @Override
    @SuppressWarnings("all")
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        String content = webSocketMessage.getPayload().toString();
        log.info("Received WS message: {}", content);
        BaseWsRequest payload;
        try {
            payload = objectMapper.readValue(content, BaseWsRequest.class);
        } catch (Exception e) {
            log.warn("Couldn't deserialize input WS message: " + content + " for WS session: " + webSocketSession, e);
            return;
        }
        try {
            payload.setWsSessionId(webSocketSession.getId());
            handleWsRequest(payload);
            log.info("Payload={}", payload);
        } catch (Exception ex) {
            log.error("Couldn't handle request: " + payload, ex);
        }
    }

    @SneakyThrows
    private void handleWsRequest(BaseWsRequest payload) {
        if (payload instanceof UpdateNote updateNote) {
            NoteDto noteDto = new NoteDto(updateNote.getNoteId(), updateNote.getNoteText(), updateNote.getUserIdentifier());
            redisTemplate.opsForValue().set("note_"+updateNote.getNoteId(), objectMapper.writeValueAsString(noteDto));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        log.error("Error in ws session: " + webSocketSession, throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        String wsiKey = "wsi_" + webSocketSession.getId();
        long userId = Long.parseLong(redisTemplate.opsForValue().get(wsiKey));
        Set<String> keys = redisTemplate.keys("note_*");
        log.info("Keys={}", keys);
        for (String key : keys) {
            NoteDto updateNote = objectMapper.readValue(redisTemplate.opsForValue().get(key), NoteDto.class);
            log.info("Object={}", updateNote);
            if (userId == updateNote.userId()) {
                Note note = noteRepository.findById(updateNote.noteId()).orElseThrow();
                note.setText(updateNote.noteText());
                noteRepository.save(note);
                redisTemplate.delete("note_"+note.getId());
            }
        }

        Boolean delete = redisTemplate.delete(wsiKey);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}