package com.workhelper.domain.auth.dto;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

public class AuthDto { // 필요하다면 클래스명을 AuthRequest로 변경하셔도 좋습니다.

    // 1. 일반 사용자 DTO
    @Getter
    @NoArgsConstructor // 👈 스프링(Jackson) 역직렬화를 위해 필수!
    @AllArgsConstructor
    @Builder
    public static class Signup {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효하지 않은 이메일 형식입니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @NotBlank(message = "이름은 필수입니다.")
        private String name;
    }

// 💡 노무사 회원가입용 폼 데이터 DTO (multipart용)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpertSignup {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효하지 않은 이메일 형식입니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @NotBlank(message = "이름은 필수입니다.")
        private String name;

        private String licenseNumber;
        private String organization;
        private MultipartFile licenseFile; 
    }

    // 2. 로그인용 내부 DTO
    @Getter
    @NoArgsConstructor // 👈 필수!
    @AllArgsConstructor
    @Builder
    public static class Login {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효하지 않은 이메일 형식입니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }

    // 3. 💡 공통 응답 DTO (회원가입 및 로그인 성공 시 반환할 데이터)
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long userId;
        private Long expertId;       // 노무사인 경우에만 존재 (일반 회원은 null)
        private String email;
        private String name;
        private String role;         // 예: "USER", "EXPERT"
        private String expertStatus; // 예: "PENDING", "APPROVED" 등 (노무사인 경우)
    }    
}