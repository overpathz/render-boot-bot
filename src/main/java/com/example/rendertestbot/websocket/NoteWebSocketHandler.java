package com.example.rendertestbot.websocket;

import com.example.rendertestbot.entity.Note;
import com.example.rendertestbot.entity.NoteToken;
import com.example.rendertestbot.repository.NoteRepository;
import com.example.rendertestbot.repository.NoteTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class NoteWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final NoteRepository noteRepository;
    private final NoteTokenRepository noteTokenRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established: {}", session.getId());
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        Long userId = getUserIdFromQuery(query);
        String token = getTokenFromQuery(query);

        if (token != null) {
            validateToken(token, userId);
        } else {
            log.warn("Token is missing or invalid for WebSocket session: {}", session.getId());
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        redisTemplate.opsForValue().set("wsi_" + session.getId(), String.valueOf(userId));
    }

    private void validateToken(String token, Long userId) {
        NoteToken noteToken = noteTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid token"));

        if (!noteToken.getUserId().equals(String.valueOf(userId))) {
            throw new IllegalStateException("Token does not match userId");
        }

        if (noteToken.getExpiryTime().isBefore(Instant.now())) {
            throw new IllegalStateException("Token is expired");
        }

        log.info("Token validated successfully for userId: {}", userId);
    }

    private Long getUserIdFromQuery(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("userId=")) {
                return Long.valueOf(param.split("=")[1]);
            }
        }
        throw new IllegalArgumentException("Missing userId in WebSocket query");
    }

    private String getTokenFromQuery(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.split("=")[1];
            }
        }
        return null;
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
        if (payload instanceof UpdateNoteWsRequest updateNoteWsRequest) {
            NoteDto noteDto = new NoteDto(updateNoteWsRequest.getNoteId(), updateNoteWsRequest.getNoteText(), updateNoteWsRequest.getUserIdentifier());
            redisTemplate.opsForValue().set("note_"+ updateNoteWsRequest.getNoteId(), objectMapper.writeValueAsString(noteDto));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        log.error("Error in ws session: " + webSocketSession, throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionKey = "wsi_" + session.getId();
        try {
            Long userId = Long.parseLong(redisTemplate.opsForValue().get(sessionKey));
            Set<String> keys = redisTemplate.keys("note_*");

            for (String key : keys) {
                NoteDto noteDto = objectMapper.readValue(redisTemplate.opsForValue().get(key), NoteDto.class);
                if (noteDto.userId().equals(userId)) {
                    Note note = noteRepository.findById(noteDto.noteId()).orElseThrow();
                    note.setText(noteDto.noteText());
                    noteRepository.save(note);
                    redisTemplate.delete(key);
                }
            }
            redisTemplate.delete(sessionKey);
        } catch (Exception e) {
            log.error("Error during cleanup for session: {}", session.getId(), e);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}