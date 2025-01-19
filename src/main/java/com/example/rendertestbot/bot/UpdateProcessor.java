package com.example.rendertestbot.bot;

import com.example.rendertestbot.entity.NoteToken;
import com.example.rendertestbot.entity.Note;
import com.example.rendertestbot.repository.NoteRepository;
import com.example.rendertestbot.repository.NoteTokenRepository;
import com.example.rendertestbot.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UpdateProcessor {
    private final TgMessageSender messageSender;
    private final NoteRepository noteRepository;
    private final UrlShortenerService urlShortenerService;
    private final NoteTokenRepository noteTokenRepository;

    public void processUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText();

            if (text.contains("#")) {
                String[] topicAndText = text.split("_");
                Note note = new Note();
                note.setText(topicAndText[1]);
                note.setTopic(topicAndText[0]);
                note.setUserId(update.getMessage().getFrom().getId());
                note.setUniqueLinkId(RandomStringUtils.randomAlphanumeric(16));
                noteRepository.save(note);
            } else if (text.contains("all")) {
                String collect = noteRepository.findAll().stream().map(Note::getId).map(String::valueOf).collect(Collectors.joining(","));
                SendMessage sendMessage = SendMessage.builder()
                    .text(collect)
                    .chatId(message.getChatId())
                    .build();
                messageSender.sendMessage(sendMessage);
            } else if (text.contains("get")) {
                // get [id]
                String[] commandAndId = text.split(" ");
                long id = Long.parseLong(commandAndId[1]);
                Note note = noteRepository.findById(id).orElseThrow();
                String uniqueLinkId = note.getUniqueLinkId();
                SendMessage sendMessage = SendMessage.builder()
                        .text(urlShortenerService.shortenUrl("http://localhost:8080/api/v1/notes?uniqueLinkId="+uniqueLinkId))
                        .chatId(message.getChatId())
                        .build();
                messageSender.sendMessage(sendMessage);
            } else if (text.contains("edit")) {
                // edit [id]
                String[] commandAndId = text.split(" ");
                long id = Long.parseLong(commandAndId[1]);
                Note note = noteRepository.findById(id).orElseThrow();

                String token = UUID.randomUUID().toString();
                NoteToken noteToken = new NoteToken();
                noteToken.setToken(token);
                noteToken.setNoteId(id);
                noteToken.setExpiryTime(Instant.now().plus(Duration.ofMinutes(10)));
                noteToken.setUserId(String.valueOf(note.getUserId()));
                noteTokenRepository.save(noteToken);

                String uniqueLinkId = note.getUniqueLinkId();

                String editLink = urlShortenerService.shortenUrl(
                        "http://localhost:8080/note/" + uniqueLinkId + "/edit?token=" + token
                );

                SendMessage sendMessage = SendMessage.builder()
                        .text(editLink)
                        .chatId(message.getChatId())
                        .build();
                messageSender.sendMessage(sendMessage);
            }
        }
    }
}
