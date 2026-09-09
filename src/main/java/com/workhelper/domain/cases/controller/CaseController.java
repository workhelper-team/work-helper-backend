package com.workhelper.domain.cases.controller;

import com.workhelper.domain.cases.dto.CaseCreateRequest;
import com.workhelper.domain.cases.dto.CaseResponse;
import com.workhelper.domain.cases.entity.Case;
import com.workhelper.domain.cases.service.CaseService;
import com.workhelper.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    @PostMapping
    public ResponseEntity<ApiResponse<CaseResponse>> createCase(
            @Valid @RequestBody CaseCreateRequest request) {
        CaseResponse response = caseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<ApiResponse<CaseResponse>> getCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(ApiResponse.success(caseService.getCase(caseId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CaseResponse>>> getCasesByUser(
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(caseService.getCasesByUser(userId)));
    }

    @PatchMapping("/{caseId}/status")
    public ResponseEntity<ApiResponse<CaseResponse>> updateStatus(
            @PathVariable Long caseId,
            @RequestParam Case.CaseStatus status) {
        return ResponseEntity.ok(ApiResponse.success(caseService.updateStatus(caseId, status)));
    }
}
