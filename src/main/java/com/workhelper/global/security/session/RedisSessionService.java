package com.workhelper.global.security.session;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisSessionService {

    private static final DefaultRedisScript<Long> SAVE_SESSION_SCRIPT = new DefaultRedisScript<>(
        "redis.call('HSET', KEYS[1], 'userId', ARGV[1], 'sessionId', ARGV[2]) " +
            "return redis.call('PEXPIRE', KEYS[1], ARGV[3])",
        Long.class);
    private static final DefaultRedisScript<Long> ROTATE_SESSION_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('HGET', KEYS[1], 'sessionId') ~= ARGV[1] then return 0 end " +
            "redis.call('HSET', KEYS[1], 'userId', ARGV[2], 'sessionId', ARGV[3]) " +
            "redis.call('PEXPIRE', KEYS[1], ARGV[4]) return 1",
        Long.class);
    private static final DefaultRedisScript<Long> DELETE_SESSION_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('HGET', KEYS[1], 'sessionId') ~= ARGV[1] then return 0 end " +
            "return redis.call('DEL', KEYS[1])",
        Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisSessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(Long userId, String sessionId, long ttlMilliseconds) {
        redisTemplate.execute(
            SAVE_SESSION_SCRIPT,
            List.of(sessionKey(userId)),
            userId.toString(),
            sessionId,
            Long.toString(ttlMilliseconds));
    }

    public boolean isSessionValid(Long userId, String sessionId) {
        List<Object> values = redisTemplate.opsForHash()
            .multiGet(sessionKey(userId), List.of("userId", "sessionId"));

        if (values == null || values.size() != 2) {
            return false;
        }

        return userId.toString().equals(values.get(0))
            && java.util.Objects.equals(sessionId, values.get(1));
    }

    public boolean rotateSession(Long userId, String currentSessionId,
                                 String newSessionId, long ttlMilliseconds) {
        Long result = redisTemplate.execute(
            ROTATE_SESSION_SCRIPT,
            List.of(sessionKey(userId)),
            currentSessionId,
            userId.toString(),
            newSessionId,
            Long.toString(ttlMilliseconds));
        return Long.valueOf(1).equals(result);
    }

    public boolean deleteSession(Long userId, String sessionId) {
        Long result = redisTemplate.execute(
            DELETE_SESSION_SCRIPT,
            List.of(sessionKey(userId)),
            sessionId);
        return Long.valueOf(1).equals(result);
    }

    private String sessionKey(Long userId) {
        return "session:" + userId;
    }
}