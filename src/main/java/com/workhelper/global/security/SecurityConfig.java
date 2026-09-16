package com.workhelper.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    //비밀번호 해시
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }    

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // REST API 서버이므로 CSRF 보호 비활성화 (토큰 기반이나 세션 쓰지 않는 경우)
            .csrf(AbstractHttpConfigurer::disable)
            // 폼 로그인 및 기본 HTTP Basic 인증 비활성화 (우리가 만든 컨트롤러를 쓰기 위함)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // URL별 권한 관리 설정
            .authorizeHttpRequests(auth -> auth
                // /api/auth/**로 시작하는 모든 요청은 인증 없이 누구나 접근 가능
                .requestMatchers("/api/auth/**").permitAll()
                // 그 외의 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );

        return http.build();
    }    
}
