package com.workhelper.domain.legal.service;

import com.workhelper.domain.legal.dto.LegalDocumentDetailResponse;
import com.workhelper.domain.legal.dto.LegalDocumentSummaryResponse;
import com.workhelper.domain.legal.entity.LegalDocument;
import com.workhelper.domain.legal.exception.LegalDocumentExceptions.LegalDocumentNotFoundException;
import com.workhelper.domain.legal.repository.LegalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalDocumentServiceImpl implements LegalDocumentService {

    /** 목록 API의 기본 페이지 크기. 데이터 규모가 커지면 설정값으로 외부화한다. */
    private static final int PAGE_SIZE = 10;
    private static final int SNIPPET_LENGTH = 150;

    private final LegalDocumentRepository legalDocumentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LegalDocumentSummaryResponse> searchLegalDocuments(String query, String sourceType, Integer page) {
        // 음수 페이지는 Spring Data가 예외를 발생시키므로 0페이지로 보정한다.
        // 현재는 query 빈 문자열을 허용하므로 전체 검색처럼 동작할 수 있다.
        int pageNumber = (page == null || page < 0) ? 0 : page;
        PageRequest pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "legalDocumentId"));

        return legalDocumentRepository.search(query, sourceType, pageRequest)
                .map(this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LegalDocumentDetailResponse getLegalDocumentDetail(Long legalDocumentId) {
        LegalDocument document = legalDocumentRepository.findById(legalDocumentId)
                .orElseThrow(() -> new LegalDocumentNotFoundException(legalDocumentId));

        return new LegalDocumentDetailResponse(
                document.getLegalDocumentId(),
                document.getTitle(),
                document.getFullText(),
                document.getSourceType(),
                document.getSourceId(),
                document.getSourceUrl()
        );
    }

    private LegalDocumentSummaryResponse toSummaryResponse(LegalDocument document) {
        // 현재 Repository는 엔티티 전체를 조회한 뒤 요약 응답으로 변환한다.
        // fullText와 metadata가 커지면 목록 전용 Projection으로 필요한 컬럼만 조회하는 것이 좋다.
        return new LegalDocumentSummaryResponse(
                document.getLegalDocumentId(),
                document.getTitle(),
                document.getSourceType(),
                truncate(document.getFullText(), SNIPPET_LENGTH),
                document.getSourceUrl()
        );
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}