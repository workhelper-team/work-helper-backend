package com.workhelper.domain.laborcase.dto;

import com.workhelper.domain.laborcase.entity.CaseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ============================================================
// 노동 사건 생성 요청 DTO
//
// POST /api/cases
//
// 클라이언트가 사건을 생성할 때 전달하는 데이터
// ============================================================

@Getter
@NoArgsConstructor
public class LaborCaseRequestDto {

    // ========================================================
    // 사건 제목
    // 필수 / 최대 200자
    // ========================================================

    @NotBlank
    @Size(max = 200)
    private String title;

    // ========================================================
    // 사건 카테고리
    // 필수
    // MVP에서는 WAGE만 허용
    // ========================================================

    @NotNull
    private CaseCategory category;

    // ========================================================
    // 사건 초기 설명
    // 선택
    //
    // 사건 생성 시 입력한 내용은
    // LaborCase.summary가 아니라
    // 첫 번째 USER ConsultationMessage로 저장
    // ========================================================

    private String initialDescription;
}