package com.droneiq.telemetry.websocket;

import com.droneiq.auth.service.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(TelemetryWebSocketHandler.class);
    private static final long AUTH_TIMEOUT_SECONDS = 5;

    private final JwtService jwtService;
    private final TelemetryWebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    // Track sessions that have completed first-message auth
    private final Map<String, Boolean> authenticatedSessionMap = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> authTimeoutTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TelemetryWebSocketHandler(
            JwtService jwtService,
            TelemetryWebSocketService webSocketService,
            ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New WebSocket connection established: session={}", session.getId());
        authenticatedSessionMap.put(session.getId(), false);

        // Schedule authentication timeout (closes connection if no valid auth message received in 5 seconds)
        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            Boolean isAuthenticated = authenticatedSessionMap.get(session.getId());
            if (isAuthenticated != null && !isAuthenticated) {
                log.warn("WebSocket session {} timed out waiting for authentication. Closing connection.", session.getId());
                try {
                    sendJson(session, Map.of("type", "ERROR", "message", "Authentication timeout"));
                    session.close(CloseStatus.POLICY_VIOLATION);
                } catch (IOException e) {
                    log.debug("Error closing timed out session: {}", e.getMessage());
                }
            }
        }, AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        authTimeoutTasks.put(session.getId(), timeoutTask);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(payload);
        } catch (Exception e) {
            sendJson(session, Map.of("type", "ERROR", "message", "Invalid JSON format"));
            return;
        }

        String messageType = jsonNode.has("type") ? jsonNode.get("type").asText() : "";
        boolean isAuthenticated = Boolean.TRUE.equals(authenticatedSessionMap.get(session.getId()));

        if (!isAuthenticated) {
            if ("AUTH".equalsIgnoreCase(messageType)) {
                handleAuth(session, jsonNode);
            } else {
                sendJson(session, Map.of("type", "AUTH_REQUIRED", "message", "Authentication required as first message"));
            }
            return;
        }

        // Session is authenticated: handle commands
        switch (messageType.toUpperCase()) {
            case "SUBSCRIBE" -> {
                String droneId = jsonNode.has("droneId") ? jsonNode.get("droneId").asText() : null;
                if (droneId != null && !droneId.isBlank()) {
                    webSocketService.subscribe(session, droneId);
                    sendJson(session, Map.of("type", "SUBSCRIBED", "droneId", droneId));
                } else {
                    sendJson(session, Map.of("type", "ERROR", "message", "droneId is required for SUBSCRIBE"));
                }
            }
            case "UNSUBSCRIBE" -> {
                String droneId = jsonNode.has("droneId") ? jsonNode.get("droneId").asText() : null;
                if (droneId != null) {
                    webSocketService.unsubscribe(session, droneId);
                    sendJson(session, Map.of("type", "UNSUBSCRIBED", "droneId", droneId));
                }
            }
            case "SUBSCRIBE_ALL" -> {
                webSocketService.subscribeAll(session);
                sendJson(session, Map.of("type", "SUBSCRIBED_ALL"));
            }
            case "PING" -> sendJson(session, Map.of("type", "PONG"));
            default -> sendJson(session, Map.of("type", "UNKNOWN_TYPE", "requested", messageType));
        }
    }

    private void handleAuth(WebSocketSession session, JsonNode jsonNode) throws IOException {
        String token = jsonNode.has("token") ? jsonNode.get("token").asText() : null;

        if (token != null && jwtService.validateToken(token)) {
            String username = jwtService.extractUsername(token);
            authenticatedSessionMap.put(session.getId(), true);

            // Cancel timeout task
            ScheduledFuture<?> task = authTimeoutTasks.remove(session.getId());
            if (task != null) {
                task.cancel(false);
            }

            webSocketService.registerSession(session);
            // Default to subscribe all unless client selects specific drones
            webSocketService.subscribeAll(session);

            log.info("WebSocket session {} authenticated as user: {}", session.getId(), username);
            sendJson(session, Map.of(
                    "type", "AUTH_SUCCESS",
                    "username", username,
                    "message", "Authenticated successfully. Subscribed to all drone telemetry by default."
            ));
        } else {
            log.warn("WebSocket session {} failed authentication: invalid token", session.getId());
            sendJson(session, Map.of("type", "AUTH_FAILURE", "message", "Invalid or expired token"));
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: session={}, status={}", session.getId(), status);
        authenticatedSessionMap.remove(session.getId());
        ScheduledFuture<?> task = authTimeoutTasks.remove(session.getId());
        if (task != null) {
            task.cancel(false);
        }
        webSocketService.unregisterSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    private void sendJson(WebSocketSession session, Map<String, Object> data) throws IOException {
        synchronized (session) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));
            }
        }
    }
}
