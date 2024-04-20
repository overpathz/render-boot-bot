package com.example.rendertestbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class RenderTestBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String tgUsername;

    public RenderTestBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    private UpdateProcessor updateProcessor;

    @Override
    public void onUpdateReceived(Update update) {
        updateProcessor.processUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return tgUsername;
    }

    public void setUpdateProcessor(UpdateProcessor updateProcessor) {
        this.updateProcessor = updateProcessor;
    }
}
