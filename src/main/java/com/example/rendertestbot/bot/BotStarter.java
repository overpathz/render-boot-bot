package com.example.rendertestbot.bot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Primary
public class BotStarter {
    private final BotRegistrar botRegistrar;
    private final RenderTestBot renderTestBot;
    private final UpdateProcessor updateProcessor;

    @PostConstruct
    public void start() throws TelegramApiException {
        renderTestBot.setUpdateProcessor(updateProcessor);
        botRegistrar.register(renderTestBot);
    }
}
