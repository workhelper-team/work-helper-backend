package com.workhelper.domain.laborcase.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;


// ============================================================
// 노동 사건 요청 DTO
// 클라이언트가 노동 사건을 생성하거나 수정할 때
// 서버로 전달하는 데이터를 담는 객체
// ============================================================
@Getter
@NoArgsConstructor
public class LaborCaseRequestDto {

    // 노동 사건 제목
    // 예: "부당해고 관련 상담"
    private String title;

    // 노동 사건의 분류
    // 예: "부당해고", "임금체불", "직장 내 괴롭힘"
    private String category;

    // 노동 사건의 현재 상태
    // 예: "IN_PROGRESS", "COMPLETED"
    private String status;

    // 노동 사건에 대한 요약 내용
    private String summary;
}