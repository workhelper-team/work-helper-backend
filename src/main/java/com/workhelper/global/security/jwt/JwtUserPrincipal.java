package com.workhelper.global.security.jwt;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class JwtUserPrincipal implements UserDetails {

    private final Long userId;
    private final String sessionId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserPrincipal(Long userId, String email,
                            Collection<? extends GrantedAuthority> authorities) {
        this(userId, null, email, authorities);
    }

    public JwtUserPrincipal(Long userId, String sessionId, String email,
                            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.email = email;
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
