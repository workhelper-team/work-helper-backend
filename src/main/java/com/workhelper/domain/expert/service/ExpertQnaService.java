package com.workhelper.domain.expert.service;

import com.workhelper.domain.expert.dto.*;
import org.springframework.data.domain.Page;

public interface ExpertQnaService {

    // 일반 사용자가 특정 사건에 질문을 등록합니다.
    ExpertQuestionResponse createQuestion(Long userId, Long caseId, ExpertQuestionRequest request);

    // 일반 사용자가 자신의 사건에 등록된 질문 목록을 조회합니다.
    Page<ExpertQuestionSummaryResponse> getMyQuestions(Long userId, Long caseId, int page, int size);

    // 일반 사용자가 자신의 사건에 등록된 질문 하나를 상세 조회합니다.
    ExpertQuestionDetailResponse getMyQuestionDetail(Long userId, Long caseId, Long questionId);

    // 승인된 전문가가 답변할 질문 목록을 조회합니다.
    Page<ExpertQuestionSummaryResponse> getQuestionsForExpert(Long userId, String status, int page, int size);

    // 승인된 전문가가 질문 하나의 상세 내용과 답변 목록을 조회합니다.
    ExpertQuestionDetailResponse getQuestionDetailForExpert(Long userId, Long questionId);

    // 승인된 전문가가 질문에 답변을 등록합니다.
    ExpertAnswerResponse createAnswer(Long userId, Long questionId, ExpertAnswerRequest request);
}