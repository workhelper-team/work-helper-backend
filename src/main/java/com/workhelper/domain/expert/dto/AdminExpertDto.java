package com.workhelper.domain.expert.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.time.LocalDateTime;

public class AdminExpertDto {

    @Getter
    @Setter
    public static class Response {
        private Long expertId;
        private String name; // Users 테이블 조인 또는 데이터 구조에 맞게 매핑
        private String email;
        private String organization;
        private String licenseNumber;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class StatusUpdateResponse {
        private Long expertId;
        private String status;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class StatusUpdateRequest {
        private List<Long> expertIds; // 복수 선택(1명이상) 체크박스 처리를 위한 리스트
        private String status;        // APPROVED 또는 REJECTED
    }
}