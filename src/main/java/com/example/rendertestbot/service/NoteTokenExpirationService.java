package com.example.rendertestbot.service;

import com.example.rendertestbot.repository.NoteTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NoteTokenExpirationService {

    private final NoteTokenRepository tokenRepository;

    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanUpExpiredTokens() {
        tokenRepository.deleteAllByExpiryTimeBefore(Instant.now());
    }
}
