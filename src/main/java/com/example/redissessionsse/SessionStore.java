package com.example.redissessionsse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class SessionStore {
    private final ConcurrentHashMap<String, Long> users = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentHashMap<Long, SessionUser> sessions = new ConcurrentHashMap<>();
    private final UserHashDbService userHashDbService;
    private final RestTemplate restTemplate;

    public synchronized void addConn(long sessionId, String userId, SessionUser sessionUser) {
        // first, check in local store
        if (users.containsKey(userId)) {
            long oldSessionId = users.get(userId);
            killConn(oldSessionId);
        }
        // delete old connections in other pod
        userHashDbService.findKeysByUserId(userId).forEach(this::killRemoteConn);
        users.put(userId, sessionId);
        sessions.put(sessionId, sessionUser);
        userHashDbService.save(sessionUser.userHash());
        System.out.println("Add connection: " + sessionId);
    }

    public void sendHeartbeat(long sessionId) {
        isDisconnected(sessionId);
    }

    public boolean isDisconnected(long sessionId) {
        try {
            sessions.get(sessionId).emitter()
                    .send(SseEmitter.event().name("heartbeat").data("heartbeat"));
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public synchronized void killConn(long sessionId) {
        SessionUser oldSessionUser = sessions.get(sessionId);
        try {
            oldSessionUser.emitter()
                    .send(SseEmitter.event()
                            .name("error").data("A new connection started"));
            oldSessionUser.emitter().complete();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        removeConn(sessionId);
    }

    public synchronized void removeConn(long sessionId) {
        String userId = sessions.get(sessionId).userId();
        users.remove(userId);
        sessions.remove(sessionId);
        userHashDbService.deleteById("user:" + userId + ":" + sessionId);
        System.out.println("Remove connection: " + sessionId);
    }

    @Scheduled(fixedRate = 5_000)
    public void checkHeartbeat() {
        sessions.forEach((sessionId, sessionUser) -> {
            if (isDisconnected(sessionId)) {
                removeConn(sessionId);
            }
        });
    }

    @Scheduled(fixedRate = 10_000)
    public void printStore() {
        System.out.println("Users: ");
        users.values().forEach(System.out::println);
        System.out.println("Sessions: ");
        sessions.values().forEach(System.out::println);
    }

    public void broadcast(String message) {
        sessions.forEach((sessionId, sessionUser) -> {
            try {
                sessionUser.emitter()
                        .send(SseEmitter.event().name(message).data(message));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
    }

    private void killRemoteConn(final String hashId) {
        CompletableFuture.runAsync(() -> {
            UserHash userHash = userHashDbService.getByHashId(hashId);
            if (userHash == null || userHash.podId().equals(System.getenv("POD_IP"))) {
                return;
            }
            // call pod to remoteKill older connection
            System.out.println("Kill connection: " + userHash);
            String url = UriComponentsBuilder.fromHttpUrl("http://" + userHash.podId() + ":7000/kill")
                    .queryParam("sessionId", userHash.sessionId())
                    .toUriString();
            try {
                restTemplate.getForEntity(url, String.class);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }
}
