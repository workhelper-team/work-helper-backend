package com.workhelper.domain.laborcase.dto;

import com.workhelper.domain.laborcase.entity.CaseCategory;
import com.workhelper.domain.laborcase.entity.CaseStatus;
import com.workhelper.domain.laborcase.entity.LaborCase;
import lombok.Getter;

// ============================================================
// 노동 사건 응답 DTO
//
// 서버에서 조회하거나 생성-수정한 노동 사건 정보를
// 클라이언트에게 반환할 때 사용하는 데이터 객체
// ============================================================

@Getter
public class LaborCaseResponseDto {

    // 노동 사건의 고유 ID
    private Long caseId;

    // 노동 사건 제목
    private String title;

    // 노동 사건 분류
    private CaseCategory category;

    // 노동 사건 현재 상태
    private CaseStatus status;

    // 노동 사건 요약
    private String summary;

    // ============================================================
    // LaborCase Entity → Response DTO 변환
    // ============================================================

    public LaborCaseResponseDto(LaborCase laborCase) {

        // Entity의 사건 ID
        this.caseId = laborCase.getCaseId();

        // Entity의 사건 제목
        this.title = laborCase.getTitle();

        // Entity의 사건 분류
        this.category = laborCase.getCategory();

        // Entity의 사건 상태
        this.status = laborCase.getStatus();

        // Entity의 사건 요약
        this.summary = laborCase.getSummary();
    }
}