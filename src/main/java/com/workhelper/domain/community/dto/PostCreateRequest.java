package com.workhelper.domain.community.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 게시글 작성 요청 DTO
 * (작성자 식별은 인증 적용 전까지 별도 파라미터로 전달)
 */
public record PostCreateRequest(

        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
}
