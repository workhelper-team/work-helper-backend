package com.workhelper.domain.consultation.controller;

import com.workhelper.domain.consultation.dto.ConsultationMessageRequestDto;
import com.workhelper.domain.consultation.dto.ConsultationMessageResponseDto;
import com.workhelper.domain.consultation.service.ConsultationMessageService;
import com.workhelper.global.security.jwt.JwtUserPrincipal;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ============================================================
// 상담 메시지 REST API Controller
//
// 클라이언트의 HTTP 요청을 받아 Service로 전달하고
// 처리 결과를 HTTP 응답으로 반환
// ============================================================

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cases/{caseId}/messages")
public class ConsultationMessageController {

    private final ConsultationMessageService consultationMessageService;

    // ============================================================
    // 상담 메시지 조회 API
    //
    // GET /api/cases/{caseId}/messages
    // ============================================================

    @GetMapping
    public ResponseEntity<List<ConsultationMessageResponseDto>> getMessages(
            @PathVariable Long caseId,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {

        Long userId = principal.getUserId();

        List<ConsultationMessageResponseDto> response =
                consultationMessageService.getMessages(caseId, userId);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 상담 메시지 전송 API
    //
    // POST /api/cases/{caseId}/messages
    // ============================================================

    @PostMapping
    public ResponseEntity<ConsultationMessageResponseDto> sendMessage(
            @PathVariable Long caseId,
            @Valid @RequestBody ConsultationMessageRequestDto requestDto,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {

        Long userId = principal.getUserId();

        ConsultationMessageResponseDto response =
                consultationMessageService.sendMessage(
                        caseId,
                        userId,
                        requestDto
                );

        return ResponseEntity.ok(response);
    }
}