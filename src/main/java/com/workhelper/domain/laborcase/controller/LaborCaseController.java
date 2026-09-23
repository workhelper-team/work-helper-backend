package com.workhelper.domain.laborcase.controller;

import com.workhelper.domain.laborcase.dto.LaborCaseRequestDto;
import com.workhelper.domain.laborcase.dto.LaborCaseResponseDto;
import com.workhelper.domain.laborcase.dto.LaborCaseUpdateRequestDto;
import com.workhelper.domain.laborcase.service.LaborCaseService;
import com.workhelper.global.security.jwt.JwtUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// ============================================================
// 노동 사건 REST API Controller
//
// 노동 사건에 대한 HTTP 요청을 받아
// Service에 전달하고 처리 결과를 클라이언트에게 반환
// ============================================================

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cases")
public class LaborCaseController {

    private final LaborCaseService laborCaseService;

    // ============================================================
    // 노동 사건 생성 API
    //
    // POST /api/cases
    // ============================================================
    @PostMapping
    public ResponseEntity<LaborCaseResponseDto> createCase(
            @Valid @RequestBody LaborCaseRequestDto requestDto,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        Long userId = principal.getUserId();

        LaborCaseResponseDto response =
                laborCaseService.createCase(userId, requestDto);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 노동 사건 목록 조회 API
    //
    // GET /api/cases
    //
    // status / page / size
    // ============================================================
    @GetMapping
    public ResponseEntity<Page<LaborCaseResponseDto>> getAllCases(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        Long userId = principal.getUserId();

        Page<LaborCaseResponseDto> response =
                laborCaseService.getCases(
                        userId,
                        status,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 특정 노동 사건 조회 API
    //
    // GET /api/cases/{caseId}
    // ============================================================
    @GetMapping("/{caseId}")
    public ResponseEntity<LaborCaseResponseDto> getCase(
            @PathVariable Long caseId,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        Long userId = principal.getUserId();

        LaborCaseResponseDto response =
                laborCaseService.getCase(caseId, userId);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 노동 사건 수정 API
    //
    // PATCH /api/cases/{caseId}
    // ============================================================
    @PatchMapping("/{caseId}")
    public ResponseEntity<LaborCaseResponseDto> updateCase(
            @PathVariable Long caseId,
            @RequestBody LaborCaseUpdateRequestDto requestDto,
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        Long userId = principal.getUserId();

        LaborCaseResponseDto response =
                laborCaseService.updateCase(
                        caseId,
                        userId,
                        requestDto
                );

        return ResponseEntity.ok(response);
    }
}