package com.workhelper.domain.laborcase.controller;

import com.workhelper.domain.laborcase.dto.LaborCaseRequestDto;
import com.workhelper.domain.laborcase.dto.LaborCaseResponseDto;
import com.workhelper.domain.laborcase.service.LaborCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// ============================================================
// 노동 사건 Controller
// 노동 사건 관련 HTTP 요청을 받아 Service로 전달하고
// 처리 결과를 클라이언트에게 반환
// ============================================================
@RestController

// 노동 사건 API의 기본 URL
// 예: /api/cases
@RequestMapping("/api/cases")

// final 필드인 Service를 생성자를 통해 자동 주입
@RequiredArgsConstructor
public class LaborCaseController {

    // 노동 사건 관련 비즈니스 로직을 처리하는 Service
    private final LaborCaseService laborCaseService;


    // ============================================================
    // 노동 사건 생성 API
    // POST /api/cases
    // ============================================================
    @PostMapping
    public ResponseEntity<LaborCaseResponseDto> createCase(
            // 클라이언트가 보낸 사건 정보를 Request DTO로 전달받음
            @RequestBody LaborCaseRequestDto requestDto) {

        // Service에 사건 생성 요청 전달
        LaborCaseResponseDto response =
                laborCaseService.createCase(requestDto);

        // 생성된 사건 정보를 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }


    // ============================================================
    // 전체 노동 사건 조회 API
    // GET /api/cases
    // ============================================================
    @GetMapping
    public ResponseEntity<List<LaborCaseResponseDto>> getAllCases() {

        // Service를 통해 전체 사건 목록 조회
        List<LaborCaseResponseDto> response =
                laborCaseService.getAllCases();

        // 조회한 사건 목록을 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }


    // ============================================================
    // 특정 노동 사건 조회 API
    // GET /api/cases/{id}
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<LaborCaseResponseDto> getCase(
            // URL의 {id} 값을 사건 ID로 전달받음
            @PathVariable Long id) {

        // 해당 ID의 사건을 Service에서 조회
        LaborCaseResponseDto response =
                laborCaseService.getCase(id);

        // 조회한 사건 정보를 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }


    // ============================================================
    // 노동 사건 수정 API
    // PUT /api/cases/{id}
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<LaborCaseResponseDto> updateCase(
            // 수정할 사건의 ID
            @PathVariable Long id,

            // 클라이언트가 전달한 수정 데이터를 Request DTO로 받음
            @RequestBody LaborCaseRequestDto requestDto) {

        // Service에 사건 ID와 수정 데이터를 전달
        LaborCaseResponseDto response =
                laborCaseService.updateCase(id, requestDto);

        // 수정된 사건 정보를 HTTP 200 OK로 반환
        return ResponseEntity.ok(response);
    }


    // ============================================================
    // 노동 사건 삭제 API
    // DELETE /api/cases/{id}
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCase(
            // 삭제할 사건의 ID
            @PathVariable Long id) {

        // Service를 통해 해당 사건 삭제
        laborCaseService.deleteCase(id);

        // 삭제가 정상적으로 처리되었음을 HTTP 200 OK로 반환
        return ResponseEntity.ok().build();
    }
}