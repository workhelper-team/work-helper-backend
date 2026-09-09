package com.workhelper.domain.petition.entity;

import com.workhelper.domain.cases.entity.Case;
import com.workhelper.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 생성 진정서 저장/관리
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "petitions")
public class Petition extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseEntity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PetitionStatus status;

    public enum PetitionStatus {
        DRAFT,      // AI 생성 초안
        REVIEWED,   // 사용자 검토 완료
        SUBMITTED   // 제출 완료
    }

    @Builder
    public Petition(Case caseEntity, String title, String content, PetitionStatus status) {
        this.caseEntity = caseEntity;
        this.title = title;
        this.content = content;
        this.status = status != null ? status : PetitionStatus.DRAFT;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateStatus(PetitionStatus status) {
        this.status = status;
    }
}
