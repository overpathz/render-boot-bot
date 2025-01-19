package com.example.rendertestbot.redis;

import com.example.rendertestbot.websocket.NoteDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisCache {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void add(String chatId, String data) {
        redisTemplate.opsForValue().set(chatId, data);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public String remove(String key) {
        return redisTemplate.opsForValue().getAndDelete(key);
    }

    public void saveUserSession(String sessionId, Long userId) {
        String redisKey = CacheKeysName.USER_WS_SESSION_ID.replace("{sessionId}", sessionId);
        redisTemplate.opsForValue().set(redisKey, String.valueOf(userId));
    }

    public Long getUserBySession(String sessionId) {
        String redisKey = CacheKeysName.USER_WS_SESSION_ID.replace("{sessionId}", sessionId);
        String userIdStr = redisTemplate.opsForValue().get(redisKey);
        return userIdStr != null ? Long.valueOf(userIdStr) : null;
    }

    public void saveNote(NoteDto noteDto) throws Exception {
        String redisKey = CacheKeysName.NOTE_ID.replace("{noteId}", String.valueOf(noteDto.noteId()));
        String noteJson = objectMapper.writeValueAsString(noteDto);
        redisTemplate.opsForValue().set(redisKey, noteJson);
    }

    public NoteDto getNoteById(Long noteId) throws Exception {
        String redisKey = CacheKeysName.NOTE_ID.replace("{noteId}", String.valueOf(noteId));
        String noteJson = redisTemplate.opsForValue().get(redisKey);
        return noteJson != null ? objectMapper.readValue(noteJson, NoteDto.class) : null;
    }

    public static class CacheKeysName {
        public static final String USER_WS_SESSION_ID = "user:ws:session:{sessionId}";
        public static final String NOTE_ID = "user:note:{noteId}";
    }
}
