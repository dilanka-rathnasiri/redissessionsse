package com.example.redissessionsse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SseEventHandler {
    private final SessionStore sessionStore;

    @Async
    @Scheduled(fixedRate = 1000)
    public void sendEventBmw() {
        sessionStore.broadcast("bmw");
    }

    @Async
    @Scheduled(fixedRate = 2000)
    public void sendEventMustang() {
        sessionStore.broadcast("Mustang");
    }

    @Async
    @Scheduled(fixedRate = 3000)
    public void sendEventPagani() {
        sessionStore.broadcast("Pagani");
    }

    @Async
    @Scheduled(fixedRate = 4000)
    public void sendEventToyota() {
        sessionStore.broadcast("Toyota");
    }
}
