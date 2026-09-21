package com.workhelper.domain.expert.repository;

import com.workhelper.domain.expert.entity.ExpertAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpertAnswerRepository extends JpaRepository<ExpertAnswer, Long> {

    // 특정 질문에 달린 모든 답변을 조회합니다.
    List<ExpertAnswer> findByExpertQuestion_QuestionId(Long questionId);

    long countByExpertQuestion_QuestionId(Long questionId);

    // 같은 전문가가 같은 질문에 이미 답변했는지 확인합니다.
    boolean existsByExpertQuestion_QuestionIdAndExpertProfile_ExpertId(Long questionId, Long expertId);
}