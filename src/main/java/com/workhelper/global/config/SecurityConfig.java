package com.workhelper.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.workhelper.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Bean
    //비밀번호 해시
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }    

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // REST API 서버이므로 CSRF 보호 비활성화 (토큰 기반이나 세션 쓰지 않는 경우)
            .csrf(csrf -> csrf.disable())
            // Spring Security 기본 로그인 폼 비활성화
            .formLogin(form -> form.disable())
            // HTTP Basic 인증 비활성화
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
            // 로그인, 회원가입 등 인증이 필요 없는 경로는 permitAll 설정
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")) // 관리자 API는 ADMIN만
            //.anyRequest().authenticated())  막힐 수 있으니 주석 처리
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }    
}
