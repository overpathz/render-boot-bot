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

            if (text.equalsIgnoreCase("/start")) {
                handleStartCommand(message);
            } else if (text.contains("#")) {
                handleSaveNoteCommand(message, text);
            } else if (text.equalsIgnoreCase("show my notes")) {
                handleShowMyNotesCommand(message);
            } else if (text.startsWith("get")) {
                handleGetNoteCommand(message, text);
            } else if (text.startsWith("edit")) {
                handleEditNoteCommand(message, text);
            } else {
                handleUnknownCommand(message);
            }
        }
    }

    private void handleStartCommand(Message message) {
        String instructions = """
            Welcome to the NoteBot! Here are the commands you can use:
            1. #topic_text - Save a new note (e.g., #work_Today's meeting notes).
            2. get [id] - Get a note by its ID (e.g., get 123).
            3. edit [id] - Edit a note by its ID (e.g., edit 123).
            4. show my notes - Show IDs of all notes you have created.
            """;

        SendMessage sendMessage = SendMessage.builder()
                .text(instructions)
                .chatId(message.getChatId())
                .build();
        messageSender.sendMessage(sendMessage);
    }

    private void handleSaveNoteCommand(Message message, String text) {
        try {
            String[] topicAndText = text.split("_", 2);
            if (topicAndText.length < 2) {
                sendErrorMessage(message, "Invalid format. Use: #topic_text");
                return;
            }

            Note note = new Note();
            note.setText(topicAndText[1]);
            note.setTopic(topicAndText[0]);
            note.setUserId(message.getFrom().getId());
            note.setUniqueLinkId(RandomStringUtils.randomAlphanumeric(16));
            Note savedNote = noteRepository.save(note);

            SendMessage sendMessage = SendMessage.builder()
                    .text("Note saved successfully! Note ID: " + savedNote.getId())
                    .chatId(message.getChatId())
                    .build();
            messageSender.sendMessage(sendMessage);
        } catch (Exception e) {
            sendErrorMessage(message, "An error occurred while saving your note.");
        }
    }

    private void handleShowMyNotesCommand(Message message) {
        Long userId = message.getFrom().getId();
        String notes = noteRepository.getAllByUserId(userId).stream()
                .map(note -> "ID: " + note.getId() + ", Topic: " + note.getTopic())
                .collect(Collectors.joining("\n"));

        if (notes.isEmpty()) {
            notes = "You have no notes.";
        }

        SendMessage sendMessage = SendMessage.builder()
                .text(notes)
                .chatId(message.getChatId())
                .build();
        messageSender.sendMessage(sendMessage);
    }

    private void handleGetNoteCommand(Message message, String text) {
        try {
            String[] commandAndId = text.split(" ");
            if (commandAndId.length < 2) {
                sendErrorMessage(message, "Invalid format. Use: get [id]");
                return;
            }

            long id = Long.parseLong(commandAndId[1]);
            Note note = noteRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Note not found"));

            Long userIdFromMessage = message.getFrom().getId();
            if (!note.getUserId().equals(userIdFromMessage)) {
                sendErrorMessage(message, "Unauthorized access: This note does not belong to you.");
                return;
            }

            String uniqueLinkId = note.getUniqueLinkId();
            SendMessage sendMessage = SendMessage.builder()
                    .text(urlShortenerService.shortenUrl("http://localhost:8080/api/v1/notes?uniqueLinkId=" + uniqueLinkId))
                    .chatId(message.getChatId())
                    .build();
            messageSender.sendMessage(sendMessage);
        } catch (Exception e) {
            sendErrorMessage(message, "An error occurred while retrieving the note.");
        }
    }

    private void handleEditNoteCommand(Message message, String text) {
        try {
            String[] commandAndId = text.split(" ");
            if (commandAndId.length < 2) {
                sendErrorMessage(message, "Invalid format. Use: edit [id]");
                return;
            }

            long id = Long.parseLong(commandAndId[1]);
            Note note = noteRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Note not found"));

            Long userIdFromMessage = message.getFrom().getId();
            if (!note.getUserId().equals(userIdFromMessage)) {
                sendErrorMessage(message, "Unauthorized access: This note does not belong to you.");
                return;
            }

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
                    .text("Edit your note using this link: " + editLink)
                    .chatId(message.getChatId())
                    .build();
            messageSender.sendMessage(sendMessage);
        } catch (Exception e) {
            sendErrorMessage(message, "An error occurred while generating the edit link.");
        }
    }


    private void handleUnknownCommand(Message message) {
        SendMessage sendMessage = SendMessage.builder()
                .text("Unknown command. Use /start to see the list of available commands.")
                .chatId(message.getChatId())
                .build();
        messageSender.sendMessage(sendMessage);
    }

    private void sendErrorMessage(Message message, String error) {
        SendMessage sendMessage = SendMessage.builder()
                .text(error)
                .chatId(message.getChatId())
                .build();
        messageSender.sendMessage(sendMessage);
    }
}
