package com.workhelper.domain.consultation.dto;

import com.workhelper.domain.consultation.entity.ConsultationMessage;
import lombok.Getter;

import java.time.OffsetDateTime;


// ============================================================
// 상담 메시지 응답 DTO
// 상담 메시지 API에서 클라이언트에게 전달할 응답 데이터를 담당
// ============================================================
@Getter
public class ConsultationMessageResponseDto {

    // ============================================================
    // 상담 메시지 식별자
    // DB의 message_id에 해당
    // ============================================================
    private final Long messageId;

    // ============================================================
    // 메시지를 보낸 역할
    // 정의서 기준 USER / ASSISTANT
    // ============================================================
    private final String role;

    // ============================================================
    // 화면에 표시할 메시지 내용
    // 사용자 질문 또는 AI 답변
    // ============================================================
    private final String content;

    // ============================================================
    // AI 답변의 구조화된 결과
    // 쟁점, 근거, 불확실성, 추가확인,
    // 대응, 출처 등의 정보를 포함
    // 사용자 메시지인 경우 NULL
    // ============================================================
    private final String structuredResult;

    // ============================================================
    // 메시지 생성 시각
    // DB의 created_at에 해당
    // ============================================================
    private final OffsetDateTime createdAt;


    // ============================================================
    // Entity → Response DTO 변환
    // API 응답에 필요한 데이터만 DTO로 전달
    // ============================================================
    public ConsultationMessageResponseDto(ConsultationMessage message) {

        this.messageId = message.getId();
        this.role = message.getRole();
        this.content = message.getContent();
        this.structuredResult = message.getStructuredResult();
        this.createdAt = message.getCreatedAt();
    }
}