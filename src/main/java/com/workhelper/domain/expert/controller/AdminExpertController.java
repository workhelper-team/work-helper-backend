package com.workhelper.domain.expert.controller;

import com.workhelper.domain.expert.dto.AdminExpertDto;
import com.workhelper.domain.expert.service.AdminExpertService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/experts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminExpertController {

    private final AdminExpertService adminExpertService;

    // 노무사 가입 신청 목록 조회 (페이징 제외)
    @GetMapping
    public ResponseEntity<List<AdminExpertDto.Response>> getExpertApplications() {
        List<AdminExpertDto.Response> responses = adminExpertService.getExpertApplications();
        return ResponseEntity.ok(responses);
    }

    // 노무사 가입 신청 상세 조회
    @GetMapping("/{expertId}")
    public ResponseEntity<AdminExpertDto.Response> getExpertDetail(@PathVariable Long expertId) {
        AdminExpertDto.Response response = adminExpertService.getExpertDetail(expertId);
        return ResponseEntity.ok(response);
    }

    // API-ADM-003: 노무사 자격증 사본 조회
    @GetMapping("/{expertId}/license-file")
    public ResponseEntity<Resource> getLicenseFile(@PathVariable Long expertId) {
        Resource fileResource = adminExpertService.loadLicenseFile(expertId);
        return ResponseEntity.ok(fileResource);
    }

    // API-ADM-004: 노무사 가입 승인/거절 (복수 선택 처리)
    @PatchMapping("/status")
    public ResponseEntity<List<AdminExpertDto.StatusUpdateResponse>> updateExpertStatuses(
            @RequestBody AdminExpertDto.StatusUpdateRequest request) {
        List<AdminExpertDto.StatusUpdateResponse> responses = adminExpertService.updateExpertStatuses(
                request.getExpertIds(), request.getStatus());
        return ResponseEntity.ok(responses);
    }
}