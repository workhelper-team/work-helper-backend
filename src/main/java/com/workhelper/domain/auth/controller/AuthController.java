package com.workhelper.domain.auth.controller;

import com.workhelper.domain.auth.dto.AuthDto;
import com.workhelper.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 1. 일반 사용자 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthDto.Response> signup(@RequestBody @Valid AuthDto.Signup dto) {
        AuthDto.Response response = authService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

/**
     * 2. 노무사 회원가입 (multipart/form-data 처리)
     */
    @PostMapping(value = "/expert-signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthDto.Response> expertSignup(@ModelAttribute @Valid AuthDto.ExpertSignup dto) {
        AuthDto.Response response = authService.expertSignup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 3. 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDto.Response> login(@RequestBody @Valid AuthDto.Login dto) {
        AuthDto.Response response = authService.login(dto);
        return ResponseEntity.ok(response);
    }
}