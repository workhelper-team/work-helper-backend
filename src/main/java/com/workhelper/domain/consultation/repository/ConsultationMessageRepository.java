package com.workhelper.domain.consultation.repository;

import com.workhelper.domain.consultation.entity.ConsultationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// ==========================================================
// 상담 메시지 Repository
// consultation_messages 테이블에 접근하여
// 상담 메시지 저장 및 조회를 담당
// ==========================================================
public interface ConsultationMessageRepository
        extends JpaRepository<ConsultationMessage, Long> {

    // ======================================================
    // 특정 노동 사건에 연결된 상담 메시지 조회
    //
    // laborCase의 id를 기준으로 조회하고
    // createdAt 오름차순으로 정렬하여
    // 실제 상담 대화 순서대로 반환
    // ======================================================
    List<ConsultationMessage> findByLaborCase_IdOrderByCreatedAtAsc(
            Long laborCaseId
    );
}