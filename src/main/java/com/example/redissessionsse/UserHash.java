package com.example.redissessionsse;

public record UserHash(String hashId, long sessionId, String userId, String podId) {
    public UserHash(long sessionId, String userId) {
        this("user:" + userId + ":" + sessionId, sessionId, userId, getPodId());
    }

    private static String getPodId() {
        String podId = System.getenv("POD_IP");
        return podId != null ? podId : "pod-0";
    }
}
