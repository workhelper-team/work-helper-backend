package com.workhelper.domain.expert.entity;

import com.workhelper.domain.laborcase.entity.LaborCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "expert_questions", schema = "app")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 일반 사용자가 사건에 대해 노무사에게 등록하는 질문 Entity입니다.
 */
public class ExpertQuestion {

    // 질문을 식별하는 데이터베이스 기본 키입니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    // 질문이 속한 사건입니다. 질문 하나는 반드시 하나의 사건에 연결됩니다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LaborCase laborCase;

    // 사용자가 작성한 질문 제목입니다.
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // 사용자가 작성한 질문 본문입니다.
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 답변 등록 여부를 나타내는 질문 상태입니다.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuestionStatus status;

    // 질문이 처음 저장된 시간입니다. 저장 후에는 변경하지 않습니다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // 질문 정보가 마지막으로 수정된 시간입니다.
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Builder
    public ExpertQuestion(LaborCase laborCase, String title, String content) {
        // Builder가 전달한 값으로 질문을 만들고, 새 질문은 항상 대기 상태로 시작합니다.
        this.laborCase = laborCase;
        this.title = title;
        this.content = content;
        this.status = QuestionStatus.WAITING;
    }

    @PrePersist
    protected void onCreate() {
        // JPA가 INSERT 직전에 호출합니다.
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        // JPA가 UPDATE 직전에 호출합니다.
        this.updatedAt = OffsetDateTime.now();
    }

    /** 최초 답변이 등록될 때만 WAITING -> ANSWERED로 전이합니다. */
    public void markAnswered() {
        // 서비스가 최초 답변 등록 시 호출하는 상태 변경 메서드입니다.
        this.status = QuestionStatus.ANSWERED;
    }
}