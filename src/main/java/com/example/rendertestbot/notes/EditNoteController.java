package com.example.rendertestbot.notes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EditNoteController {
    private final NoteRepository noteRepository;

    @GetMapping("/note/{uniqueNoteId}/edit")
    public ModelAndView editNote(@PathVariable String uniqueNoteId, ModelAndView modelAndView) {
        Note note = noteRepository.getNoteByUniqueLinkId(uniqueNoteId).orElseThrow();
        modelAndView.addObject("note", note);
        modelAndView.setViewName("editNote");
        return modelAndView;
    }
}
