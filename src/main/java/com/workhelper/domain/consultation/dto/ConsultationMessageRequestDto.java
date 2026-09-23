package com.workhelper.domain.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ============================================================
// 상담 메시지 요청 DTO
// 상담 메시지 전송 API에서 클라이언트가 전달하는
// 요청 데이터를 담는 객체
// ============================================================

@Getter
@NoArgsConstructor
public class ConsultationMessageRequestDto {

    // ============================================================
    // 상담 메시지 내용
    //
    // 클라이언트가 입력한 사용자 상담 내용을 전달받음
    // 빈 문자열이나 공백만 입력된 경우 validation 실패
    // Service에서 ConsultationMessage의 content로 사용
    // ============================================================

    @NotBlank
    private String content;

}

