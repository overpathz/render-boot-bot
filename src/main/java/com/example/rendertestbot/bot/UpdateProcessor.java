package com.example.rendertestbot.bot;

import com.example.rendertestbot.entity.Note;
import com.example.rendertestbot.entity.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UpdateProcessor {
    private final TgMessageSender messageSender;
    private final NoteRepository noteRepository;

    public void processUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText();

            if (text.contains("#")) {
                String[] topicAndText = text.split("_");
                Note note = new Note();
                note.setText(topicAndText[1]);
                note.setTopic(topicAndText[0]);
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
                        .text("http://localhost:8080/api/v1/notes?uniqueLinkId="+uniqueLinkId)
                        .chatId(message.getChatId())
                        .build();
                messageSender.sendMessage(sendMessage);
            }
        }
    }
}
