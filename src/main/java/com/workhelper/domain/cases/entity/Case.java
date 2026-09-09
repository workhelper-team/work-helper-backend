package com.workhelper.domain.cases.entity;

import com.workhelper.domain.user.entity.User;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "cases")
public class Case extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String caseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CaseStatus status;

    public enum CaseStatus {
        RECEIVED,    // 접수됨
        IN_PROGRESS, // 진행 중
        PENDING,     // 보류
        RESOLVED,    // 해결됨
        CLOSED       // 종료
    }

    @Builder
    public Case(User user, String title, String description, String caseType, CaseStatus status) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.caseType = caseType;
        this.status = status != null ? status : CaseStatus.RECEIVED;
    }

    public void updateStatus(CaseStatus status) {
        this.status = status;
    }
}
