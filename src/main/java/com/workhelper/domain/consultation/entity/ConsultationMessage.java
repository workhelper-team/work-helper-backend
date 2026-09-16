package com.workhelper.domain.consultation.entity;

import com.workhelper.domain.laborcase.entity.LaborCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


// 상담 메시지 Entity
// DB의 consultation_messages 테이블과 연결되는 클래스
@Entity
@Table(name = "consultation_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationMessage {

    // ============================================================
    // 상담 메시지 고유 ID
    // DB에서 자동으로 번호를 생성
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ============================================================
    // 상담 메시지가 어떤 노동 사건에 속해 있는지 연결
    // 하나의 사건(LaborCase)에 여러 개의 상담 메시지가 존재할 수 있음
    // ============================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labor_case_id", nullable = false)
    private LaborCase laborCase;


    // ============================================================
    // 메시지를 보낸 주체
    // 예: USER / AI
    // ============================================================
    @Column(name = "sender_type", nullable = false)
    private String senderType;


    // ============================================================
    // 실제 상담 메시지 내용
    // TEXT 타입으로 저장하여 긴 상담 내용도 저장할 수 있도록 설정
    // ============================================================
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;


    // ============================================================
    // 상담 내용을 AI가 분석한 구조화 결과
    // 예: 상담 요약, 사건 분류 등의 결과를 저장
    // ============================================================
    @Column(name = "structured_result", columnDefinition = "TEXT")
    private String structuredResult;


    // ============================================================
    // Entity 객체 생성용 Builder
    // 필요한 값들을 지정하여 ConsultationMessage 객체를 생성
    // ============================================================
    @Builder
    public ConsultationMessage(
            LaborCase laborCase,
            String senderType,
            String content,
            String structuredResult
    ) {
        this.laborCase = laborCase;
        this.senderType = senderType;
        this.content = content;
        this.structuredResult = structuredResult;
    }
}