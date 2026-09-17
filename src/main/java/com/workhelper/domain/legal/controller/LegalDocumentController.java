package com.workhelper.domain.legal.controller;

import com.workhelper.domain.legal.dto.LegalDocumentDetailResponse;
import com.workhelper.domain.legal.dto.LegalDocumentSummaryResponse;
import com.workhelper.domain.legal.service.LegalDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;

    /**
     * API-LEGAL-001: 법률자료 검색
     * 인증은 global.security의 전역 설정(예: Spring Security에서 "/api/**" 인증 요구)으로
     * 처리된다고 가정 — 이 API는 사건 소유권 같은 개별 검증이 필요 없어 별도 인증 코드는 없음.
     */
    @GetMapping
    public ResponseEntity<Page<LegalDocumentSummaryResponse>> searchLegalDocuments(
            @RequestParam String query,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Integer page
    ) {
        return ResponseEntity.ok(legalDocumentService.searchLegalDocuments(query, sourceType, page));
    }

    /**
     * API-LEGAL-002: 법률자료 상세 조회
     */
    @GetMapping("/{legalDocumentId}")
    public ResponseEntity<LegalDocumentDetailResponse> getLegalDocumentDetail(
            @PathVariable Long legalDocumentId
    ) {
        return ResponseEntity.ok(legalDocumentService.getLegalDocumentDetail(legalDocumentId));
    }
}