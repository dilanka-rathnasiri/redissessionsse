package com.example.redissessionsse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public record SessionUser(long sessionId, String userId, SseEmitter emitter, UserHash userHash) {
}
