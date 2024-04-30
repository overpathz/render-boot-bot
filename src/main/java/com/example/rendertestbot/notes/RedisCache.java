package com.example.rendertestbot.notes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
@Slf4j
@RequiredArgsConstructor
public class RedisCache {

    private final RedisTemplate<String, String> redisTemplate;

    public void add(String chatId, String data) {
        redisTemplate.opsForValue().set(chatId, data);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public String remove(String key) {
        return redisTemplate.opsForValue().getAndDelete(key);
    }
}
