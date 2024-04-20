package com.example.rendertestbot.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class UpdateProcessor {
    private final TgMessageSender messageSender;

    public void processUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText();
            SendMessage sendMessage = SendMessage.builder()
                    .text(text)
                    .chatId(message.getChatId())
                    .build();
            messageSender.sendMessage(sendMessage);
        }
    }
}
