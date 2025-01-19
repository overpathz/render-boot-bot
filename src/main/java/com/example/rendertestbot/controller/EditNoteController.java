package com.example.rendertestbot.controller;

import com.example.rendertestbot.entity.Note;
import com.example.rendertestbot.repository.NoteRepository;
import com.example.rendertestbot.repository.NoteTokenRepository;
import com.example.rendertestbot.entity.NoteToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EditNoteController {
    private final NoteRepository noteRepository;
    private final NoteTokenRepository tokenRepository;

    @GetMapping("/note/{uniqueNoteId}/edit")
    public ModelAndView editNote(@PathVariable String uniqueNoteId,
                                 @RequestParam String token,
                                 ModelAndView modelAndView) {

        NoteToken noteToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token"));

        if (!noteToken.getExpiryTime().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token expired");
        }

        Note note = noteRepository.getNoteByUniqueLinkId(uniqueNoteId).orElseThrow();

        modelAndView.addObject("note", note);
        modelAndView.setViewName("editNote");
        return modelAndView;
    }
}
