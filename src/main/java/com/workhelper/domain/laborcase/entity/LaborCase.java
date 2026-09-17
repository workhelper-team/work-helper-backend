package com.workhelper.domain.laborcase.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

// ============================================================
// 노동 사건 Entity
// Java의 LaborCase 객체와 DB의 cases 테이블을 연결
// ============================================================
@Entity
@Table(name = "cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LaborCase {

    // ============================================================
    // 노동 사건 고유 ID
    // DB: case_id
    // PK / IDENTITY
    //
    // 사건이 저장될 때 DB에서 ID 자동 생성
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private Long id;

    // ============================================================
    // 사건 제목
    // DB: title
    // VARCHAR(200) / NOT NULL
    // ============================================================
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // ============================================================
    // 사건 카테고리
    // DB: category
    // VARCHAR(50)
    // ============================================================
    @Column(name = "category", length = 50)
    private String category;

    // ============================================================
    // 사건 상태
    // DB: status
    // VARCHAR(30) / NOT NULL
    //
    // 상태값은 설계서에서 정의된 값을 사용하며
    // Entity에서 임의의 기본값을 지정하지 않음
    // ============================================================
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    // ============================================================
    // 사건 요약
    // DB: summary
    // TEXT
    // ============================================================
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    // ============================================================
    // 사건 생성 일시
    // DB: created_at
    // TIMESTAMPTZ / NOT NULL
    // DB 기본값: CURRENT_TIMESTAMP
    // ============================================================
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // ============================================================
    // 사건 수정 일시
    // DB: updated_at
    // TIMESTAMPTZ / NOT NULL
    // DB 기본값: CURRENT_TIMESTAMP
    // ============================================================
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ============================================================
    // Entity 생성용 Builder
    //
    // DB에서 자동 생성되는 id와 생성/수정 일시는 받지 않음
    // ============================================================
    @Builder
    public LaborCase(
            String title,
            String category,
            String status,
            String summary
    ) {
        this.title = title;
        this.category = category;
        this.status = status;
        this.summary = summary;
    }

    // ============================================================
    // 기존 노동 사건 정보 수정
    //
    // 전달된 값이 null이 아닌 경우 해당 필드만 수정
    // ============================================================
    public void updateCase(
            String title,
            String category,
            String status,
            String summary
    ) {
        // 제목이 전달된 경우 수정
        if (title != null) {
            this.title = title;
        }

        // 카테고리가 전달된 경우 수정
        if (category != null) {
            this.category = category;
        }

        // 상태가 전달된 경우 수정
        if (status != null) {
            this.status = status;
        }

        // 요약이 전달된 경우 수정
        if (summary != null) {
            this.summary = summary;
        }
    }
}