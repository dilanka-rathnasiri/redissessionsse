package com.example.redissessionsse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserHashDbService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void save(UserHash userHash) {
        redisTemplate.opsForHash().putAll(userHash.hashId(), Utils.convertUserHashToMap(userHash));
    }

    public Set<String> findKeysByUserId(String userId) {
        return redisTemplate.keys( "user:" + userId + ":*");
    }

    public UserHash getByHashId(String hashId) {
        return Utils.convertMapToUserHash(redisTemplate.opsForHash().entries(hashId));
    }

    public void deleteById(String hashId) {
        redisTemplate.delete(hashId);
    }
}
