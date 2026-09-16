package com.workhelper.domain.laborcase.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


// ============================================================
// 노동 사건 Entity
// Java의 LaborCase 객체와 DB의 cases 테이블을 연결
// ============================================================
@Entity
@Table(name = "cases")
@Getter

// JPA에서 Entity 객체를 생성할 때 사용하는 기본 생성자
// 외부에서 직접 호출하지 못하도록 protected로 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LaborCase {


    // ============================================================
    // 노동 사건 고유 ID
    // DB에서 새로운 사건이 생성될 때 자동으로 번호 생성
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ============================================================
    // 사건 제목
    // 반드시 값이 존재해야 하므로 nullable = false 설정
    // ============================================================
    @Column(nullable = false)
    private String title;


    // ============================================================
    // 사건 카테고리
    // 예: 부당해고, 임금체불, 직장 내 괴롭힘 등
    // ============================================================
    private String category;


    // ============================================================
    // 사건 상태
    // 예: IN_PROGRESS, COMPLETED
    // 반드시 값이 존재해야 하므로 nullable = false 설정
    // ============================================================
    @Column(nullable = false)
    private String status;


    // ============================================================
    // 사건 요약
    // 긴 텍스트를 저장할 수 있도록 TEXT 타입으로 설정
    // ============================================================
    @Column(columnDefinition = "TEXT")
    private String summary;


    // ============================================================
    // Builder를 이용한 Entity 생성
    // Service에서 필요한 값만 지정하여 LaborCase 객체 생성 가능
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

        // 상태가 전달되지 않은 경우 기본값으로 IN_PROGRESS 사용
        this.status = status != null ? status : "IN_PROGRESS";

        this.summary = summary;
    }


    // ============================================================
    // 기존 노동 사건 정보 수정
    // null이 아닌 값만 수정하여 기존 데이터를 유지
    // ============================================================
    public void updateCase(
            String title,
            String category,
            String status,
            String summary
    ) {

        // 제목이 전달된 경우 제목 수정
        if (title != null) this.title = title;

        // 카테고리가 전달된 경우 카테고리 수정
        if (category != null) this.category = category;

        // 상태가 전달된 경우 상태 수정
        if (status != null) this.status = status;

        // 요약이 전달된 경우 요약 수정
        if (summary != null) this.summary = summary;
    }
}