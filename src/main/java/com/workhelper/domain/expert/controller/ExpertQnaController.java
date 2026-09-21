package com.workhelper.domain.expert.controller;

import com.workhelper.domain.expert.dto.*;
import com.workhelper.domain.expert.service.ExpertQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expert/questions")
@RequiredArgsConstructor
/**
 * 승인된 노무사의 질문 목록/상세 조회 및 답변 등록 API 입구입니다. (F-QA-003~005)
 * @AuthenticationPrincipal Long userId는 임시 가정입니다.
 *    공통 인증 유틸이 확정되면 이 파라미터 타입만 교체예정.
 */
public class ExpertQnaController {

    // 질문 조회와 답변 등록의 실제 업무 규칙은 서비스가 담당합니다.
    private final ExpertQnaService expertQnaService;

    // 목록 API가 사용하는 페이지 응답 형식입니다.
    public record PageResult<T>(
            // 현재 페이지의 데이터입니다.
            List<T> content,
            // 0부터 시작하는 현재 페이지 번호입니다.
            int page,
            // 요청한 페이지 크기입니다.
            int size,
            // 모든 페이지를 합친 전체 데이터 수입니다.
            long totalElements,
            // 전체 페이지 수입니다.
            int totalPages,
            // 현재 페이지가 마지막인지 여부입니다.
            boolean last
    ) {
        // Spring Data Page를 외부 응답 형식으로 변환합니다.
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
    public ResponseEntity<PageResult<ExpertQuestionSummaryResponse>> getQuestionsForExpert(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 인증 객체에서 받은 사용자 식별자를 서비스에 전달합니다.
        validatePagination(page, size);
        Page<ExpertQuestionSummaryResponse> pageData = expertQnaService.getQuestionsForExpert(userId, page, size);
        return ResponseEntity.ok(PageResult.from(pageData));
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<ExpertQuestionDetailResponse> getQuestionDetailForExpert(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long questionId
    ) {
        // 전문가 권한을 확인한 뒤 질문 하나의 상세 정보를 반환합니다.
        return ResponseEntity.ok(expertQnaService.getQuestionDetailForExpert(userId, questionId));
    }

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<ExpertAnswerResponse> createAnswer(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long questionId,
            @RequestBody ExpertAnswerRequest request
    ) {
        // 전문가 권한을 확인한 뒤 해당 질문에 답변을 등록합니다.
        ExpertAnswerResponse response = expertQnaService.createAnswer(userId, questionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page는 0 이상, size는 1 이상 100 이하이어야 합니다.");
        }
    }
}