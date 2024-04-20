package com.example.rendertestbot.bot;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TgMessageSender {
    private RenderTestBot communityBot;

    public void sendTextMessage(Long userId, String message) {
        log.info("Sending text message to={}", userId);
        executeSendMessage(userId, message);
    }

    public void sendMessage(SendMessage message) {
        try {
            communityBot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Unable to send a message", e);
        }
    }

    private void executeSendMessage(Long userId, String message) {
        try {
            communityBot.execute(SendMessage.builder()
                    .chatId(userId)
                    .text(message)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException("Unable to send a message", e);
        }
    }

    @Autowired
    public void setCommunityBot(RenderTestBot communityBot) {
        this.communityBot = communityBot;
    }
}
