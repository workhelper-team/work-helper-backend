package com.workhelper.domain.auth.service;

import com.workhelper.domain.auth.dto.AuthDto;
import com.workhelper.domain.expert.repository.ExpertRepository;
import com.workhelper.domain.user.entity.User;
import com.workhelper.domain.user.repository.UserRepository;
import com.workhelper.global.security.jwt.JwtProvider;
import com.workhelper.global.security.jwt.JwtUserPrincipal;
import com.workhelper.global.security.session.RedisSessionService;
import com.workhelper.infra.storage.EvidenceStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthSessionTest {
    private final UserRepository users = mock(UserRepository.class);
    private final ExpertRepository experts = mock(ExpertRepository.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final JwtProvider jwt = mock(JwtProvider.class);
    private final RedisSessionService sessions = mock(RedisSessionService.class);
    private final AuthService auth = new AuthService(users, experts, passwords, jwt,
        mock(EvidenceStorageService.class), sessions);
    private final User user = mock(User.class);
    private final JwtUserPrincipal principal = new JwtUserPrincipal(7L, "first", "a@example.com", List.of());

    private void stubUser() {
        when(user.getUserId()).thenReturn(7L);
        when(user.getEmail()).thenReturn("a@example.com");
        when(user.getPassword()).thenReturn("hash");
        when(user.getRole()).thenReturn("GENERAL");
        when(jwt.getAccessTokenValidityInMilliseconds()).thenReturn(30000L);
        when(jwt.createToken(eq(7L), eq("a@example.com"), eq("GENERAL"), anyString()))
            .thenReturn("token");
    }

    @Test
    void loginSavesSessionBeforeReturningToken() {
        stubUser();
        when(users.findByEmail("a@example.com")).thenReturn(Optional.of(user));
        when(passwords.matches("password", "hash")).thenReturn(true);
        var login = AuthDto.Login.builder().email("a@example.com").password("password").build();
        assertThat(auth.login(login).getAccessToken()).isEqualTo("token");
        var order = inOrder(sessions, jwt);
        order.verify(sessions).saveSession(eq(7L), anyString(), eq(30000L));
        order.verify(jwt).createToken(eq(7L), eq("a@example.com"), eq("GENERAL"), anyString());
    }

    @Test
    void extendRotatesSessionAndRejectsMismatch() {
        stubUser();
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(sessions.rotateSession(eq(7L), eq("first"), anyString(), eq(30000L)))
            .thenReturn(true, false);
        assertThat(auth.extend(principal).getAccessToken()).isEqualTo("token");
        assertThatThrownBy(() -> auth.extend(principal)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void logoutDeletesCurrentSession() {
        auth.logout(principal);
        verify(sessions).deleteSession(7L, "first");
    }
}
