package com.example.redissessionsse;

import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static Map<String, Object> convertUserHashToMap(UserHash userHash) {
        Map<String, Object> map = new HashMap<>();
        map.put("hashId", userHash.hashId());
        map.put("sessionId", userHash.sessionId());
        map.put("userId", userHash.userId());
        map.put("podId", userHash.podId());
        return map;
    }

    public static UserHash convertMapToUserHash(Map<Object, Object> map) {
        return new UserHash(
                MapUtils.getString(map, "hashId"),
                MapUtils.getLong(map, "sessionId"),
                MapUtils.getString(map, "userId"),
                MapUtils.getString(map, "podId")
        );
    }
}
