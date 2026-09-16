package com.workhelper.domain.consultation.controller;

import com.workhelper.domain.consultation.dto.ConsultationRequestDto;
import com.workhelper.domain.consultation.dto.ConsultationResponseDto;
import com.workhelper.domain.consultation.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/messages")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @GetMapping
    public ResponseEntity<List<ConsultationResponseDto>> getMessages(
            @PathVariable Long caseId) {

        List<ConsultationResponseDto> response =
                consultationService.getMessages(caseId);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ConsultationResponseDto> sendMessage(
            @PathVariable Long caseId,
            @RequestBody ConsultationRequestDto requestDto) {

        ConsultationResponseDto response =
                consultationService.sendMessage(caseId, requestDto);

        return ResponseEntity.ok(response);
    }
}