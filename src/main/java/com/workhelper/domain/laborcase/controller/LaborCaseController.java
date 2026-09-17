package com.workhelper.domain.laborcase.controller;

import com.workhelper.domain.laborcase.dto.LaborCaseRequestDto;
import com.workhelper.domain.laborcase.dto.LaborCaseResponseDto;
import com.workhelper.domain.laborcase.dto.LaborCaseUpdateRequestDto;
import com.workhelper.domain.laborcase.service.LaborCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // ============================================================
    // 노동 사건 관련 비즈니스 로직을 담당하는 Service
    // ============================================================
    private final LaborCaseService laborCaseService;


    // ============================================================
    // 노동 사건 생성 API
    //
    // POST /api/cases
    //
    // 클라이언트가 전달한 사건 정보를 Service에 전달하여
    // 새로운 노동 사건을 생성
    // ============================================================
    @PostMapping
    public ResponseEntity<LaborCaseResponseDto> createCase(
            // 요청 Body의 사건 생성 정보를 전달받음
            @RequestBody LaborCaseRequestDto requestDto
    ) {
        // 사건 생성을 Service에 요청
        LaborCaseResponseDto response =
                laborCaseService.createCase(requestDto);

        // 생성된 사건 정보를 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }


    // ============================================================
    // 노동 사건 목록 조회 API
    //
    // GET /api/cases
    //
    // 사건 상태, 카테고리 등의 조건을 기준으로
    // 노동 사건 목록을 조회
    // ============================================================
    @GetMapping
    public ResponseEntity<List<LaborCaseResponseDto>> getAllCases() {

        // 사건 목록 조회를 Service에 요청
        List<LaborCaseResponseDto> response =
                laborCaseService.getCases();

        // 조회 결과를 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }


    // ============================================================
    // 특정 노동 사건 조회 API
    //
    // GET /api/cases/{caseId}
    //
    // URL의 사건 ID를 기준으로 특정 사건을 조회
    // ============================================================
    @GetMapping("/{caseId}")
    public ResponseEntity<LaborCaseResponseDto> getCase(
            // URL의 {caseId} 값을 사건 ID로 전달받음
            @PathVariable Long caseId
    ) {
        // 해당 사건 조회를 Service에 요청
        LaborCaseResponseDto response =
                laborCaseService.getCase(caseId);

        // 조회 결과를 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }


    // ============================================================
    // 노동 사건 수정 API
    //
    // PATCH /api/cases/{caseId}
    //
    // 사건 제목, 카테고리, 상태, 요약 등의 정보를
    // 필요한 항목만 선택적으로 수정
    // ============================================================
    @PatchMapping("/{caseId}")
    public ResponseEntity<LaborCaseResponseDto> updateCase(
            // 수정할 사건의 ID
            @PathVariable Long caseId,

            // 요청 Body의 수정 데이터를 전달받음
            @RequestBody LaborCaseUpdateRequestDto requestDto
    ) {
        // 사건 ID와 수정 데이터를 Service에 전달
        LaborCaseResponseDto response =
                laborCaseService.updateCase(caseId, requestDto);

        // 수정된 사건 정보를 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }
}