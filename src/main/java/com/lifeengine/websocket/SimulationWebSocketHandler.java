package com.lifeengine.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class SimulationWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }

    public void broadcast(String json) {
        TextMessage msg = new TextMessage(json);
        sessions.removeIf(s -> !s.isOpen());
        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) s.sendMessage(msg);
            } catch (Exception ignored) {}
        }
    }
}
