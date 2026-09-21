package com.workhelper.domain.expert.entity;

// ⚠️ ExpertProfile은 F-EXP-001 담당자가 이미 만들었을 가능성이 높습니다.
// import 경로와 필드명(expertId, status, user)을 실제 클래스에 맞게 확인해주세요.
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "expert_answers", schema = "app")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 승인된 노무사가 ExpertQuestion에 등록하는 답변 Entity입니다.
 * (question_id, expert_id) 조합에 DB UQ 제약이 있어 동일 노무사의 중복 답변을 막습니다.
 */
public class ExpertAnswer {

    // 답변을 식별하는 데이터베이스 기본 키입니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    // 답변이 달린 질문입니다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private ExpertQuestion expertQuestion;

    // 답변을 작성한 전문가입니다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expert_id", nullable = false)
    private ExpertProfile expertProfile;

    // 전문가가 작성한 답변 본문입니다.
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 답변이 처음 저장된 시간입니다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // 답변 정보가 마지막으로 수정된 시간입니다.
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Builder
    public ExpertAnswer(ExpertQuestion expertQuestion, ExpertProfile expertProfile, String content) {
        // Builder가 전달한 질문, 전문가, 답변 내용을 저장합니다.
        this.expertQuestion = expertQuestion;
        this.expertProfile = expertProfile;
        this.content = content;
    }

    @PrePersist
    protected void onCreate() {
        // JPA가 INSERT 직전에 호출해 생성/수정 시간을 기록합니다.
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        // JPA가 UPDATE 직전에 호출해 수정 시간을 갱신합니다.
        this.updatedAt = OffsetDateTime.now();
    }
}