package com.workhelper.domain.legal.service;

import com.workhelper.domain.legal.dto.LegalDocumentDetailResponse;
import com.workhelper.domain.legal.dto.LegalDocumentSummaryResponse;
import org.springframework.data.domain.Page;

public interface LegalDocumentService {

    /**
     * API-LEGAL-001: 법률자료 검색
     *
     * @param query      검색어 (필수)
     * @param sourceType LAW / PRECEDENT / INTERPRETATION / LABOR_COMMISSION 등 (선택)
     * @param page       0부터 시작하는 페이지 번호 (선택, 기본 0)
     */
    Page<LegalDocumentSummaryResponse> searchLegalDocuments(String query, String sourceType, Integer page);

    /**
     * API-LEGAL-002: 법률자료 상세 조회
     */
    LegalDocumentDetailResponse getLegalDocumentDetail(Long legalDocumentId);
}