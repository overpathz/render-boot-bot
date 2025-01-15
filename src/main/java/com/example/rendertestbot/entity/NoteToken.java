package com.example.rendertestbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
public class NoteToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long noteId;
    private String token;
    private String userId;
    private Instant expiryTime;

    public NoteToken(Long id, String token, Long userId, Instant plus) {
        this.id = id;
        this.token = token;
        this.userId = String.valueOf(userId);
        this.expiryTime = plus;
    }
}
