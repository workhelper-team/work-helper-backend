package com.workhelper.global.security.session;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisSessionServiceTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final HashOperations<String, Object, Object> hashes = mock(HashOperations.class);
    private final RedisSessionService sessions = new RedisSessionService(redis);

    @Test
    void savesAndValidatesSession() {
        when(redis.execute(any(RedisScript.class), eq(List.of("session:7")),
            eq("7"), eq("first"), eq("30000")))
            .thenReturn(1L);
        sessions.saveSession(7L, "first", 30000);
        verify(redis).execute(any(RedisScript.class), eq(List.of("session:7")),
            eq("7"), eq("first"), eq("30000"));

        when(redis.opsForHash()).thenReturn(hashes);
        when(hashes.multiGet("session:7", List.of("userId", "sessionId")))
            .thenReturn(List.of("7", "first"));
        assertThat(sessions.isSessionValid(7L, "first")).isTrue();
        assertThat(sessions.isSessionValid(7L, "wrong")).isFalse();
    }

    @Test
    void rotatesOnlyMatchingSessionAndDeletes() {
        when(redis.execute(any(RedisScript.class), eq(List.of("session:7")),
            eq("first"), eq("7"), eq("second"), eq("30000"))).thenReturn(1L);
        when(redis.execute(any(RedisScript.class), eq(List.of("session:7")),
            eq("wrong"), eq("7"), eq("third"), eq("30000"))).thenReturn(0L);
        when(redis.execute(any(RedisScript.class), eq(List.of("session:7")),
            eq("second"))).thenReturn(1L);
        assertThat(sessions.rotateSession(7L, "first", "second", 30000)).isTrue();
        assertThat(sessions.rotateSession(7L, "wrong", "third", 30000)).isFalse();
        assertThat(sessions.deleteSession(7L, "second")).isTrue();
    }
}
