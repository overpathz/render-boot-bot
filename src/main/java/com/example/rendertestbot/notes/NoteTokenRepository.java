package com.example.rendertestbot.notes;

import com.example.rendertestbot.entity.NoteToken;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface NoteTokenRepository extends JpaRepository<NoteToken, Long> {
    Optional<NoteToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM NoteToken t WHERE t.expiryTime < :expiryTime")
    void deleteAllByExpiryTimeBefore(@Param("expiryTime") Instant expiryTime);
}
