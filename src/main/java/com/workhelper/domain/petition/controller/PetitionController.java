package com.workhelper.domain.petition.controller;

import com.workhelper.domain.petition.dto.PetitionGenerateRequest;
import com.workhelper.domain.petition.dto.PetitionResponse;
import com.workhelper.domain.petition.service.PetitionService;
import com.workhelper.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/petitions")
@RequiredArgsConstructor
public class PetitionController {

    private final PetitionService petitionService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PetitionResponse>> generate(
            @Valid @RequestBody PetitionGenerateRequest request) {
        PetitionResponse response = petitionService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{petitionId}")
    public ResponseEntity<ApiResponse<PetitionResponse>> getPetition(
            @PathVariable Long petitionId) {
        return ResponseEntity.ok(ApiResponse.success(petitionService.getPetition(petitionId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PetitionResponse>>> getPetitionsByCase(
            @RequestParam Long caseId) {
        return ResponseEntity.ok(ApiResponse.success(petitionService.getPetitionsByCase(caseId)));
    }

    @PutMapping("/{petitionId}")
    public ResponseEntity<ApiResponse<PetitionResponse>> updateContent(
            @PathVariable Long petitionId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                ApiResponse.success(petitionService.updateContent(petitionId, body.get("content"))));
    }
}
