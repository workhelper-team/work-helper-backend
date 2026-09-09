package com.workhelper.domain.consultation.controller;

import com.workhelper.domain.consultation.dto.ChatMessageRequest;
import com.workhelper.domain.consultation.dto.ChatMessageResponse;
import com.workhelper.domain.consultation.dto.ConsultationRequest;
import com.workhelper.domain.consultation.dto.ConsultationResponse;
import com.workhelper.domain.consultation.service.ConsultationService;
import com.workhelper.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ConsultationResponse>> ask(
            @Valid @RequestBody ConsultationRequest request) {
        ConsultationResponse response = consultationService.ask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> chat(
            @RequestParam Long userId,
            @Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(consultationService.chat(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsultationResponse>>> getHistory(
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(consultationService.getHistory(userId)));
    }

    @GetMapping("/{consultationId}")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getConsultation(
            @PathVariable Long consultationId) {
        return ResponseEntity.ok(
                ApiResponse.success(consultationService.getConsultation(consultationId)));
    }
}
