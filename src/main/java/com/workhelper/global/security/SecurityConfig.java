package com.workhelper.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("local")
public class SecurityConfig {

    /**
     * 로컬 수동 테스트 전용 보안 정책이다.
     *
     * JWT 필터가 아직 구현되지 않은 단계에서 Spring Security 기본 로그인 때문에
     * Thunder Client 요청이 401이 되는 것을 막는다. 이 설정은 local 프로필에서만
     * 등록되며 운영 프로필에는 적용되지 않아야 한다.
     */
    @Bean
    SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
