package com.workhelper.domain.laborcase.dto;

import com.workhelper.domain.laborcase.entity.CaseCategory;
import com.workhelper.domain.laborcase.entity.CaseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ============================================================
// 노동 사건 수정 요청 DTO
//
// PATCH /api/cases/{caseId}
//
// 사건 정보 및 상태를 수정할 때 전달하는 데이터
// ============================================================

@Getter
@NoArgsConstructor
public class LaborCaseUpdateRequestDto {

    // ========================================================
    // 사건 제목
    // 선택
    // ========================================================

    private String title;

    // ========================================================
    // 사건 카테고리
    // 선택
    // MVP에서는 WAGE만 허용
    // ========================================================

    private CaseCategory category;

    // ========================================================
    // 사건 상태
    // 선택
    //
    // CREATED / IN_PROGRESS / CLOSED / ARCHIVED
    // ========================================================

    private CaseStatus status;

    // ========================================================
    // 사건 요약
    // 선택
    // ========================================================

    private String summary;
}