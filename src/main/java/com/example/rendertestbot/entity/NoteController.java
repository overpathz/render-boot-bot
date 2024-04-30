package com.example.rendertestbot.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notes")
public class NoteController {

    private final NoteRepository noteRepository;

    // https://localhost:8080/api/v1/notes?uniqueLinkId=sdfsfsdf

    @GetMapping
    public Note getNoteText(@RequestParam String uniqueLinkId) {
        return noteRepository.getNoteByUniqueLinkId(uniqueLinkId).orElseThrow();
    }
}
