package com.workhelper.domain.laborcase.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "labor_cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LaborCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;    // 📌 사건 제목

    private String category; // 🏷️ 사건 카테고리

    @Column(nullable = false)
    private String status;   // 📊 사건 상태

    @Column(columnDefinition = "TEXT")
    private String summary;  // 📝 사건 요약

    @Builder
    public LaborCase(String title, String category, String status, String summary) {
        this.title = title;
        this.category = category;
        this.status = status != null ? status : "IN_PROGRESS";
        this.summary = summary;
    }

    public void updateCase(String title, String category, String status, String summary) {
        if (title != null) this.title = title;
        if (category != null) this.category = category;
        if (status != null) this.status = status;
        if (summary != null) this.summary = summary;
    }
}