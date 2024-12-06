package com.example.redissessionsse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class SseController {
    private final SessionStore sessionStore;

    @GetMapping(value = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamSse(@RequestParam final String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        long sessionId = System.currentTimeMillis();
        UserHash userHash = new UserHash(sessionId, userId);
        SessionUser sessionUser = new SessionUser(sessionId, userId, emitter, userHash);
        sessionStore.addConn(sessionId, userId, sessionUser);
        emitter.onCompletion(() -> sessionStore.removeConn(sessionId));
        emitter.onTimeout(() -> sessionStore.removeConn(sessionId));
        emitter.onError(e -> sessionStore.removeConn(sessionId));
        sessionStore.sendHeartbeat(sessionId);
        return ResponseEntity.ok(emitter);
    }

    @GetMapping(value = "/pod-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getPodId() {
        HashMap<String, String> map = new HashMap<>();
        map.put("POD_IP", System.getenv("POD_IP"));
        map.put("REDIS_HOST", System.getenv("REDIS_HOST"));
        return map;
    }

    @GetMapping(value = "/kill")
    public void remoteKill(@RequestParam final long sessionId) {
        sessionStore.killConn(sessionId);
    }
}
