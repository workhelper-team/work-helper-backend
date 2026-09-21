package com.workhelper.domain.consultation.controller;

import com.workhelper.domain.consultation.dto.ConsultationMessageRequestDto;
import com.workhelper.domain.consultation.dto.ConsultationMessageResponseDto;
import com.workhelper.domain.consultation.service.ConsultationMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ============================================================
// 상담 메시지 REST API Controller
// 클라이언트의 HTTP 요청을 받아 Service로 전달하고
// 처리 결과를 HTTP 응답으로 반환
// ============================================================

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cases/{caseId}/messages")
public class ConsultationMessageController {

    // ============================================================
    // 상담 메시지 관련 비즈니스 로직을 담당하는 Service
    // ============================================================

    private final ConsultationMessageService consultationMessageService;

    // ============================================================
    // 상담 메시지 조회 API
    //
    // GET /api/cases/{caseId}/messages
    //
    // 특정 사건에 연결된 상담 메시지 목록을 조회
    // ============================================================

    @GetMapping
    public ResponseEntity<List<ConsultationMessageResponseDto>> getMessages(
            // URL의 {caseId} 값을 Long 타입으로 전달받음
            @PathVariable Long caseId
    ) {

        // 현재는 JWT userId 연결 전이므로 임시로 null 전달
        Long userId = null;

        // 특정 사건의 상담 메시지 목록을 Service에 요청
        List<ConsultationMessageResponseDto> response =
                consultationMessageService.getMessages(caseId, userId);

        // 조회 결과를 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 상담 메시지 전송 API
    //
    // POST /api/cases/{caseId}/messages
    //
    // 특정 사건에 사용자가 입력한 상담 메시지를 전달
    // ============================================================

    @PostMapping
    public ResponseEntity<ConsultationMessageResponseDto> sendMessage(
            // URL의 {caseId}를 전달받아
            // 어느 사건에 대한 상담인지 식별
            @PathVariable Long caseId,

            // 요청 Body의 메시지 내용을
            // Request DTO로 전달받음
            @Valid @RequestBody ConsultationMessageRequestDto requestDto
    ) {

        // 현재는 JWT userId 연결 전이므로 임시로 null 전달
        Long userId = null;

        // 사건 ID와 상담 메시지 내용을 Service에 전달
        ConsultationMessageResponseDto response =
                consultationMessageService.sendMessage(
                        caseId,
                        userId,
                        requestDto
                );

        // 처리 결과를 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }
}