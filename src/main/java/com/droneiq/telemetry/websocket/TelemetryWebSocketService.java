package com.droneiq.telemetry.websocket;

import com.droneiq.telemetry.dto.TelemetryMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelemetryWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryWebSocketService.class);

    private final ObjectMapper objectMapper;

    // session ID -> WebSocketSession
    private final Map<String, WebSocketSession> authenticatedSessions = new ConcurrentHashMap<>();

    // session ID -> Set of drone IDs subscribed to (empty set means subscribed to all)
    private final Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();

    // session ID -> Boolean (true if subscribed to all drones)
    private final Map<String, Boolean> subscribeAllMap = new ConcurrentHashMap<>();

    public TelemetryWebSocketService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void registerSession(WebSocketSession session) {
        authenticatedSessions.put(session.getId(), session);
        sessionSubscriptions.put(session.getId(), ConcurrentHashMap.newKeySet());
        subscribeAllMap.put(session.getId(), false);
        log.info("WebSocket session registered: {}", session.getId());
    }

    public void unregisterSession(WebSocketSession session) {
        authenticatedSessions.remove(session.getId());
        sessionSubscriptions.remove(session.getId());
        subscribeAllMap.remove(session.getId());
        log.info("WebSocket session unregistered: {}", session.getId());
    }

    public void subscribe(WebSocketSession session, String droneId) {
        Set<String> subs = sessionSubscriptions.get(session.getId());
        if (subs != null) {
            subs.add(droneId);
            log.debug("Session {} subscribed to drone {}", session.getId(), droneId);
        }
    }

    public void unsubscribe(WebSocketSession session, String droneId) {
        Set<String> subs = sessionSubscriptions.get(session.getId());
        if (subs != null) {
            subs.remove(droneId);
            log.debug("Session {} unsubscribed from drone {}", session.getId(), droneId);
        }
    }

    public void subscribeAll(WebSocketSession session) {
        subscribeAllMap.put(session.getId(), true);
        log.debug("Session {} subscribed to ALL drones", session.getId());
    }

    public void broadcastTelemetry(TelemetryMessage message) {
        if (authenticatedSessions.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "type", "TELEMETRY",
                    "droneId", message.droneId(),
                    "data", message
            );
            String json = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(json);

            for (Map.Entry<String, WebSocketSession> entry : authenticatedSessions.entrySet()) {
                String sessionId = entry.getKey();
                WebSocketSession session = entry.getValue();

                if (!session.isOpen()) {
                    continue;
                }

                boolean shouldSend = Boolean.TRUE.equals(subscribeAllMap.get(sessionId));
                if (!shouldSend) {
                    Set<String> subs = sessionSubscriptions.getOrDefault(sessionId, Collections.emptySet());
                    shouldSend = subs.contains(message.droneId());
                }

                if (shouldSend) {
                    try {
                        synchronized (session) {
                            if (session.isOpen()) {
                                session.sendMessage(textMessage);
                            }
                        }
                    } catch (IOException e) {
                        log.warn("Failed to send telemetry to session {}: {}", sessionId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize telemetry message for WebSocket broadcast: {}", e.getMessage());
        }
    }

    public int getConnectedSessionsCount() {
        return authenticatedSessions.size();
    }
}
