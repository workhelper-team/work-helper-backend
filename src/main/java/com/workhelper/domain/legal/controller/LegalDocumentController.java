package com.workhelper.domain.legal.controller;

import com.workhelper.domain.legal.dto.LegalDocumentDetailResponse;
import com.workhelper.domain.legal.dto.LegalDocumentSummaryResponse;
import com.workhelper.domain.legal.service.LegalDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/legal-documents")
@RequiredArgsConstructor
/**
 * 법률자료 외부 API를 제공합니다.
 * 검색은 외부 검색 결과를 전달받는 구조이고, 상세조회는 Spring DB 조회를 사용합니다.
 */
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;

    // Spring Page를 외부 API에서 사용하는 명시적 페이징 응답으로 변환합니다.
    public record PageResult<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {
        public static <T> PageResult<T> from(Page<T> pageData) {
            return new PageResult<>(
                    pageData.getContent(),
                    pageData.getNumber(),
                    pageData.getSize(),
                    pageData.getTotalElements(),
                    pageData.getTotalPages(),
                    pageData.isLast()
            );
        }
    }

    @GetMapping
    public ResponseEntity<PageResult<LegalDocumentSummaryResponse>> searchLegalDocuments(
            @RequestParam String query,
            @RequestParam(required = false) String sourceType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePagination(page, size);
        
        // 검색 결과를 API 응답 규격으로 변환합니다.
        Page<LegalDocumentSummaryResponse> searchResult = 
                legalDocumentService.searchLegalDocuments(query, sourceType, page, size);
                
        return ResponseEntity.ok(PageResult.from(searchResult));
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page는 0 이상, size는 1 이상 100 이하이어야 합니다.");
        }
    }

    @GetMapping("/{legalDocumentId}")
    public ResponseEntity<LegalDocumentDetailResponse> getLegalDocumentDetail(
            @PathVariable Long legalDocumentId
    ) {
        return ResponseEntity.ok(legalDocumentService.getLegalDocumentDetail(legalDocumentId));
    }
}