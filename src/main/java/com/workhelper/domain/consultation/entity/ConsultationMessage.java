package com.workhelper.domain.consultation.entity;

import com.workhelper.domain.laborcase.entity.LaborCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

// ============================================================
// 상담 메시지 Entity
//
// DB의 app.consultation_messages 테이블과 연결
// ============================================================

@Entity
@Table(name = "consultation_messages", schema = "app")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationMessage {

    // ============================================================
    // 상담 메시지 식별자
    // DB: message_id
    // BIGINT / PK / IDENTITY
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    // ============================================================
    // 해당 상담 메시지가 속한 사건
    // DB: case_id
    // app.cases.case_id FK
    // ============================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LaborCase laborCase;

    // ============================================================
    // 메시지를 보낸 역할
    // DB: role
    // USER / ASSISTANT
    // ============================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MessageRole role;

    // ============================================================
    // 메시지 내용
    // DB: content
    // ============================================================

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // ============================================================
    // AI 답변의 구조화된 결과
    // DB: structured_result
    // ============================================================

    @Column(name = "structured_result", columnDefinition = "JSONB")
    private String structuredResult;

    // ============================================================
    // 메시지 생성 시각
    // DB: created_at
    // ============================================================

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    // ============================================================
    // Entity 생성용 Builder
    // ============================================================

    @Builder
    public ConsultationMessage(
            LaborCase laborCase,
            MessageRole role,
            String content,
            String structuredResult
    ) {
        this.laborCase = laborCase;
        this.role = role;
        this.content = content;
        this.structuredResult = structuredResult;
    }
}