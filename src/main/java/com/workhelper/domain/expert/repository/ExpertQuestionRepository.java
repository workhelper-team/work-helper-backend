package com.workhelper.domain.expert.repository;

import com.workhelper.domain.expert.entity.ExpertQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpertQuestionRepository extends JpaRepository<ExpertQuestion, Long> {

    // 메서드 이름을 분석해 Spring Data JPA가 caseId 조건의 페이징 쿼리를 자동 생성합니다.
    Page<ExpertQuestion> findByLaborCase_CaseId(Long caseId, Pageable pageable);

    // 질문 ID와 사건 ID가 모두 일치하는 질문만 찾습니다.
    Optional<ExpertQuestion> findByQuestionIdAndLaborCase_CaseId(Long questionId, Long caseId);

    // 전문가용 전체 목록은 JpaRepository가 제공하는 findAll(Pageable)을 사용합니다.
}