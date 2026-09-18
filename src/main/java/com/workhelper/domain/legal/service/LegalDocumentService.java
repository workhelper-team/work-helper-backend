package com.workhelper.domain.legal.service;

import com.workhelper.domain.legal.dto.LegalDocumentDetailResponse;
import com.workhelper.domain.legal.dto.LegalDocumentSummaryResponse;
import org.springframework.data.domain.Page;

public interface LegalDocumentService {

    /**
     * 법률자료 검색 (FastAPI 검색 결과를 받아 Page 객체로 반환)
     */
    Page<LegalDocumentSummaryResponse> searchLegalDocuments(String query, String sourceType, int page, int size);

    /**
     * 법률자료 상세조회 (Spring DB 직접 조회)
     */
    LegalDocumentDetailResponse getLegalDocumentDetail(Long legalDocumentId);
}