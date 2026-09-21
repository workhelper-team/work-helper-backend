package com.workhelper.domain.expert.controller;

import com.workhelper.domain.expert.dto.*;
import com.workhelper.domain.expert.service.ExpertQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/expert-questions")
@RequiredArgsConstructor
/**
 * 일반 사용자의 전문가 Q&A 질문 등록/조회 API 입구입니다. (F-QA-001, F-QA-002)
 */
public class ExpertQuestionController {

    // 실제 질문 등록과 조회는 서비스에 맡기고, 이 클래스는 HTTP 요청과 응답을 연결합니다.
    private final ExpertQnaService expertQnaService;

    // Spring Page 객체를 API 응답에 그대로 노출하지 않기 위한 페이지 응답 형식입니다.
    public record PageResult<T>(
            // 현재 페이지에 포함된 실제 데이터 목록입니다.
            List<T> content,
            // 페이지 번호입니다. 0부터 시작합니다.
            int page,
            // 한 페이지에 요청한 데이터 개수입니다.
            int size,
            // 전체 데이터 개수입니다.
            long totalElements,
            // 전체 페이지 개수입니다.
            int totalPages,
            // 현재 페이지가 마지막 페이지인지 나타냅니다.
            boolean last
    ) {
        // Spring Data의 Page 객체를 API 응답용 PageResult로 변환합니다.
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

    @PostMapping
    public ResponseEntity<ExpertQuestionResponse> createQuestion(
            @PathVariable Long caseId,
            @RequestBody ExpertQuestionRequest request
    ) {
        // caseId와 요청 내용을 서비스에 전달하고, 생성되면 201 Created를 반환합니다.
        ExpertQuestionResponse response = expertQnaService.createQuestion(caseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResult<ExpertQuestionSummaryResponse>> getMyQuestions(
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 페이지 번호와 크기를 먼저 검사한 뒤 해당 사건의 질문 목록을 조회합니다.
        validatePagination(page, size);
        Page<ExpertQuestionSummaryResponse> pageData = expertQnaService.getMyQuestions(caseId, page, size);
        return ResponseEntity.ok(PageResult.from(pageData));
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<ExpertQuestionDetailResponse> getMyQuestionDetail(
            @PathVariable Long caseId,
            @PathVariable Long questionId
    ) {
        // caseId와 questionId가 모두 일치하는 질문의 상세 정보를 조회합니다.
        return ResponseEntity.ok(expertQnaService.getMyQuestionDetail(caseId, questionId));
    }

    // 잘못된 페이지 요청으로 DB를 조회하지 않도록 컨트롤러에서 공통 검사합니다.
    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page는 0 이상, size는 1 이상 100 이하이어야 합니다.");
        }
    }
}