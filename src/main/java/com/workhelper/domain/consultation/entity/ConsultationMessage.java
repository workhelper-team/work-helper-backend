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
// DB의 app.consultation_messages 테이블과 연결
// ============================================================
@Entity
@Table(name = "consultation_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationMessage {

    // ============================================================
    // 상담 메시지 식별자
    // DB: message_id
    // BIGINT / PK / IDENTITY
    // 메시지가 저장될 때 DB에서 ID를 자동 생성
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;


    // ============================================================
    // 해당 상담 메시지가 속한 사건
    // DB: case_id
    // app.cases.case_id를 참조하는 FK
    //
    // 하나의 사건에는 여러 개의 상담 메시지가 연결될 수 있음
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LaborCase laborCase;


    // ============================================================
    // 메시지를 보낸 역할
    // DB: role
    // VARCHAR(20) / NOT NULL
    //
    // 정의서에 명시된 값:
    // USER / ASSISTANT
    // ============================================================
    @Column(name = "role", nullable = false, length = 20)
    private String role;


    // ============================================================
    // 화면에 표시할 메시지 내용
    // DB: content
    // TEXT / NOT NULL
    //
    // 사용자 질문 또는 AI 답변을 저장
    // ============================================================
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;


    // ============================================================
    // AI 답변의 구조화된 결과
    // DB: structured_result
    // JSONB / NULL 허용
    //
    // 쟁점, 근거, 불확실성, 추가확인,
    // 대응, 출처 등의 구조화된 결과를 저장
    //
    // 사용자 메시지의 경우 NULL
    // ============================================================
    @Column(name = "structured_result", columnDefinition = "JSONB")
    private String structuredResult;


    // ============================================================
    // 메시지 생성 시각
    // DB: created_at
    // TIMESTAMPTZ / NOT NULL
    // DB 기본값: CURRENT_TIMESTAMP
    // ============================================================
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


    // ============================================================
    // Entity 생성용 Builder
    // DB에서 자동 생성되는 message_id와
    // DB 기본값을 사용하는 created_at은 받지 않음
    // ============================================================
    @Builder
    public ConsultationMessage(
            LaborCase laborCase,
            String role,
            String content,
            String structuredResult
    ) {
        this.laborCase = laborCase;
        this.role = role;
        this.content = content;
        this.structuredResult = structuredResult;
    }
}