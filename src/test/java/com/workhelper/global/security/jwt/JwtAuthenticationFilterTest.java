package com.workhelper.global.security.jwt;

import com.workhelper.global.security.session.RedisSessionService;
import com.workhelper.global.security.session.SessionStoreUnavailableException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtProvider jwt = mock(JwtProvider.class);
    private final RedisSessionService sessions = mock(RedisSessionService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, sessions);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsInvalidSessionId() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwt.validateToken("token")).thenReturn(true);
        var principal = new JwtUserPrincipal(7L, "wrong", "a@example.com", List.of());
        when(jwt.getAuthentication("token")).thenReturn(
            new UsernamePasswordAuthenticationToken(principal, "token", List.of()));
        when(sessions.isSessionValid(7L, "wrong")).thenReturn(false);

        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void returnsServiceUnavailableWhenRedisFails() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwt.validateToken("token")).thenReturn(true);
        var principal = new JwtUserPrincipal(7L, "first", "a@example.com", List.of());
        when(jwt.getAuthentication("token")).thenReturn(
            new UsernamePasswordAuthenticationToken(principal, "token", List.of()));
        when(sessions.isSessionValid(7L, "first"))
            .thenThrow(new SessionStoreUnavailableException(new RuntimeException()));

        filter.doFilter(request, response, chain);
        verify(response).sendError(503, "Session store unavailable");
        verifyNoInteractions(chain);
    }
}
