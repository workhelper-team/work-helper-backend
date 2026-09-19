package com.workhelper.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-ms:1800000}")
    private long accessTokenValidityInMilliseconds;

    private Key key;

    @PostConstruct
    protected void init() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    // Access Token 생성
    public String createToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 검증 및 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String role = claims.get("role", String.class);
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        User principal = new User(
                claims.get("email", String.class),
                "",
                Collections.singleton(new SimpleGrantedAuthority(authority)));
        return new UsernamePasswordAuthenticationToken(
            principal, token, principal.getAuthorities());
    }

    public long getAccessTokenValidityInMilliseconds() {
        return accessTokenValidityInMilliseconds;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}