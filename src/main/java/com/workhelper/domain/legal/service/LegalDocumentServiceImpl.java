package com.workhelper.domain.legal.service;

import com.workhelper.domain.legal.dto.LegalDocumentDetailResponse;
import com.workhelper.domain.legal.dto.LegalDocumentSummaryResponse;
import com.workhelper.domain.legal.entity.LegalDocument;
import com.workhelper.domain.legal.repository.LegalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 법률자료 조회 흐름을 담당합니다.
 * 검색은 FastAPI 연동 전까지 Spring에서 직접 LIKE 검색하지 않으며, 상세조회만 DB에서 수행합니다.
 */
public class LegalDocumentServiceImpl implements LegalDocumentService {

    private final LegalDocumentRepository legalDocumentRepository;

    @Override
    public Page<LegalDocumentSummaryResponse> searchLegalDocuments(String query, String sourceType, int page, int size) {
        // 실제 검색은 타팀 FastAPI Client가 담당합니다. Client 연동 전에는 빈 결과를 반환합니다.
        PageRequest pageable = PageRequest.of(page, size);
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    @Override
    public LegalDocumentDetailResponse getLegalDocumentDetail(Long legalDocumentId) {
        // 상세조회는 검색과 달리 Spring이 법률자료 DB에서 직접 조회합니다.
        LegalDocument legalDocument = legalDocumentRepository.findById(legalDocumentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 법률자료입니다."));

        return new LegalDocumentDetailResponse(
                legalDocument.getLegalDocumentId(),
                legalDocument.getTitle(),
                legalDocument.getFullText(),
                legalDocument.getSourceType(),
                legalDocument.getSourceUrl(),
                legalDocument.getMetadata()
        );
    }
}