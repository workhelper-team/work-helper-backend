package com.workhelper.domain.expert.service;

import com.workhelper.domain.expert.dto.*;
import com.workhelper.domain.expert.entity.ExpertAnswer;
import com.workhelper.domain.expert.entity.ExpertProfile;   // ⚠️ 실제 패키지 확인 필요
import com.workhelper.domain.expert.entity.ExpertQuestion;
import com.workhelper.domain.expert.entity.QuestionStatus;
import com.workhelper.domain.expert.repository.ExpertAnswerRepository;
import com.workhelper.domain.expert.repository.ExpertRepository;
import com.workhelper.domain.expert.repository.ExpertQuestionRepository;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 전문가 Q&A(F-QA-001~005)의 업무 흐름을 조정합니다.
 * 사용자용 질문 등록/조회와 노무사용 목록/상세/답변 등록을 한 서비스에서 관리합니다.
 */
public class ExpertQnaServiceImpl implements ExpertQnaService {

        // 질문을 저장하고 조회하는 Repository입니다.
    private final ExpertQuestionRepository expertQuestionRepository;
        // 답변을 저장하고 조회하는 Repository입니다.
    private final ExpertAnswerRepository expertAnswerRepository;
        // 질문이 연결될 사건을 조회하는 Repository입니다.
    private final LaborCaseRepository laborCaseRepository;
        // 로그인한 사용자가 전문가인지와 전문가 상태를 확인하는 Repository입니다.
        private final ExpertRepository expertRepository;

    // ===== 사용자용 (F-QA-001, F-QA-002) =====

    @Override
    @Transactional
        public ExpertQuestionResponse createQuestion(Long userId, Long caseId, ExpertQuestionRequest request) {
                LaborCase laborCase = laborCaseRepository.findByCaseIdAndUserId(caseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사건입니다. caseId=" + caseId));

        // 요청 DTO의 값을 Entity로 옮겨 새 질문 객체를 만듭니다.
        ExpertQuestion question = ExpertQuestion.builder()
                .laborCase(laborCase)
                .title(request.title())
                .content(request.content())
                .build();

        // Entity를 DB에 저장합니다. 저장 후에는 DB가 생성한 ID와 시간이 채워집니다.
        ExpertQuestion savedQuestion = expertQuestionRepository.save(question);

        // DB Entity를 외부 API 응답 DTO로 변환합니다.
        return new ExpertQuestionResponse(
                savedQuestion.getQuestionId(),
                caseId,
                savedQuestion.getTitle(),
                savedQuestion.getContent(),
                savedQuestion.getStatus().name(),
                savedQuestion.getCreatedAt()
        );
    }

    @Override
        public Page<ExpertQuestionSummaryResponse> getMyQuestions(Long userId, Long caseId, int page, int size) {
                laborCaseRepository.findByCaseIdAndUserId(caseId, userId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사건입니다. caseId=" + caseId));
                // PageRequest는 0부터 시작하는 페이지 번호와 한 페이지 크기를 묶습니다.
        PageRequest pageable = PageRequest.of(page, size);
                // Repository가 사건 ID에 해당하는 질문만 페이지 단위로 조회합니다.
        Page<ExpertQuestion> questionPage = expertQuestionRepository.findByLaborCase_CaseId(caseId, pageable);
                // 각 Entity를 목록용 응답 DTO로 바꿉니다.
        return questionPage.map(this::toSummaryResponse);
    }

    @Override
        public ExpertQuestionDetailResponse getMyQuestionDetail(Long userId, Long caseId, Long questionId) {
                laborCaseRepository.findByCaseIdAndUserId(caseId, userId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사건입니다. caseId=" + caseId));
                // 질문 ID뿐 아니라 사건 ID도 함께 검사해 다른 사건의 질문이 노출되지 않도록 합니다.
        ExpertQuestion question = expertQuestionRepository.findByQuestionIdAndLaborCase_CaseId(questionId, caseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사건의 질문을 찾을 수 없습니다."));

        // 질문 Entity를 상세 응답 DTO로 변환합니다.
        return toDetailResponse(question);
    }

    // ===== 전문가용 (F-QA-003, F-QA-004, F-QA-005) =====

    @Override
        public Page<ExpertQuestionSummaryResponse> getQuestionsForExpert(Long userId, String status, int page, int size) {
                // 목록을 보여주기 전에 로그인한 사용자가 승인된 전문가인지 확인합니다.
        getApprovedExpertProfile(userId); // 승인된 노무사인지만 확인

        // 현재는 모든 질문을 페이지 단위로 조회합니다.
        PageRequest pageable = PageRequest.of(page, size);
                if (status == null || status.isBlank()) {
                        return expertQuestionRepository.findAll(pageable).map(this::toSummaryResponse);
                }

                QuestionStatus questionStatus;
                try {
                        questionStatus = QuestionStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException exception) {
                        throw new IllegalArgumentException("유효하지 않은 질문 상태입니다: " + status, exception);
                }
                return expertQuestionRepository.findByStatus(questionStatus, pageable).map(this::toSummaryResponse);
    }

    @Override
    public ExpertQuestionDetailResponse getQuestionDetailForExpert(Long userId, Long questionId) {
                // 전문가 전용 API이므로 먼저 전문가 상태를 확인합니다.
        getApprovedExpertProfile(userId);

                // 질문 ID로 상세 질문을 찾습니다.
        ExpertQuestion question = expertQuestionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다. questionId=" + questionId));

        return toDetailResponse(question);
    }

    @Override
    @Transactional
    public ExpertAnswerResponse createAnswer(Long userId, Long questionId, ExpertAnswerRequest request) {
                // 답변을 작성하기 전에 로그인한 사용자가 승인된 전문가인지 확인합니다.
        ExpertProfile expertProfile = getApprovedExpertProfile(userId);

                // 답변을 등록할 질문이 존재하는지 확인합니다.
        ExpertQuestion question = expertQuestionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다. questionId=" + questionId));

        // 같은 전문가가 같은 질문에 두 번 답변하지 못하게 검사합니다.
        boolean alreadyAnswered = expertAnswerRepository
                .existsByExpertQuestion_QuestionIdAndExpertProfile_ExpertId(questionId, expertProfile.getExpertId());
        if (alreadyAnswered) {
            throw new IllegalStateException("이미 이 질문에 답변을 등록했습니다.");
        }

        // 요청 DTO의 내용을 답변 Entity로 변환합니다.
        ExpertAnswer answer = ExpertAnswer.builder()
                .expertQuestion(question)
                .expertProfile(expertProfile)
                .content(request.content())
                .build();

        // 답변을 DB에 저장합니다.
        ExpertAnswer savedAnswer = expertAnswerRepository.save(answer);

        // 질문에 달린 첫 답변이면 질문 상태도 답변 완료로 변경합니다.
        // 최초 답변인 경우에만 상태를 ANSWERED로 전이합니다.
        if (question.getStatus() == QuestionStatus.WAITING) {
            question.markAnswered();
        }

        // 저장된 답변 Entity를 API 응답 DTO로 변환합니다.
        return new ExpertAnswerResponse(
                savedAnswer.getAnswerId(),
                question.getQuestionId(),
                expertProfile.getExpertId(),
                savedAnswer.getContent(),
                savedAnswer.getCreatedAt()
        );
    }

    // ===== 공통 헬퍼 =====

    private ExpertProfile getApprovedExpertProfile(Long userId) {
                // 인증된 사용자 ID로 전문가 프로필을 찾습니다.
        // ⚠️ findByUser_UserId 메서드명은 실제 ExpertProfileRepository에 맞게 확인 필요
        ExpertProfile expertProfile = expertRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("노무사 정보를 찾을 수 없습니다."));

        // 전문가 프로필이 없거나 승인 상태가 아니면 전문가 기능을 사용할 수 없습니다.
        if (!"APPROVED".equals(expertProfile.getStatus())) {
            throw new IllegalStateException("승인된 노무사만 이용할 수 있는 기능입니다.");
        }
        return expertProfile;
    }

    private ExpertQuestionSummaryResponse toSummaryResponse(ExpertQuestion question) {
                // 목록 화면에 필요한 값만 골라 간단한 응답 DTO를 만듭니다.
        return new ExpertQuestionSummaryResponse(
                question.getQuestionId(),
                question.getLaborCase().getCaseId(),
                question.getTitle(),
                question.getStatus().name(),
                question.getLaborCase().getCategory(),
                expertAnswerRepository.countByExpertQuestion_QuestionId(question.getQuestionId()),
                question.getCreatedAt()
        );
    }

    private ExpertQuestionDetailResponse toDetailResponse(ExpertQuestion question) {
                // 질문에 연결된 답변들을 조회해 답변 응답 DTO 목록으로 변환합니다.
        List<ExpertAnswerResponse> answers = expertAnswerRepository
                .findByExpertQuestion_QuestionId(question.getQuestionId())
                .stream()
                .map(answer -> new ExpertAnswerResponse(
                        answer.getAnswerId(),
                        question.getQuestionId(),
                        answer.getExpertProfile().getExpertId(),
                        answer.getContent(),
                        answer.getCreatedAt()
                ))
                .toList();

        // 질문 본문과 답변 목록을 하나의 상세 응답으로 묶습니다.
        return new ExpertQuestionDetailResponse(
                question.getQuestionId(),
                question.getLaborCase().getCaseId(),
                question.getTitle(),
                question.getContent(),
                question.getStatus().name(),
                question.getCreatedAt(),
                answers
        );
    }
}