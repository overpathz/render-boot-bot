package com.example.rendertestbot.notes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.ExceptionWebSocketHandlerDecorator;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.socket.sockjs.transport.TransportHandler;
import org.springframework.web.socket.sockjs.transport.handler.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
@EnableWebSocket
@Slf4j
public class NoteWebSocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler handler;

    public NoteWebSocketConfig(@Lazy WebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        WebSocketHandlerRegistration registration =
                registry.addHandler(new ExceptionWebSocketHandlerDecorator(handler), "/init");
        registration.setAllowedOriginPatterns("*");
        SockJsServiceRegistration withSockJS = registration.addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS()
                .setTransportHandlers(getDefaultTransportHandlers());
        withSockJS.setSuppressCors(true)
                .setWebSocketEnabled(true)
                .setHttpMessageCacheSize(5000)
                .setHeartbeatTime(25000)
                .setClientLibraryUrl("https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js");
    }

    private TransportHandler[] getDefaultTransportHandlers() {
        Set<TransportHandler> result = new LinkedHashSet<>(6);
        result.add(new XhrPollingTransportHandler());
        result.add(new XhrReceivingTransportHandler());
        result.add(new XhrStreamingTransportHandler());
        result.add(new EventSourceTransportHandler());
        result.add(new HtmlFileTransportHandler());
        try {
            result.add(new WebSocketTransportHandler(new DefaultHandshakeHandler()));
        }
        catch (Exception ex) {
            log.warn("Failed to create a default WebSocketTransportHandler", ex);
        }
        TransportHandler[] array = new TransportHandler[result.size()];
        return result.toArray(array);
    }
}
