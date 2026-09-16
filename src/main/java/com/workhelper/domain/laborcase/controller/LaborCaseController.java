package com.workhelper.domain.laborcase.controller;

import com.workhelper.domain.laborcase.dto.LaborCaseRequestDto;
import com.workhelper.domain.laborcase.dto.LaborCaseResponseDto;
import com.workhelper.domain.laborcase.service.LaborCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class LaborCaseController {

    private final LaborCaseService laborCaseService;

    @PostMapping
    public ResponseEntity<LaborCaseResponseDto> createCase(@RequestBody LaborCaseRequestDto requestDto) {
        LaborCaseResponseDto response = laborCaseService.createCase(requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LaborCaseResponseDto>> getAllCases() {
        List<LaborCaseResponseDto> response = laborCaseService.getAllCases();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaborCaseResponseDto> getCase(@PathVariable Long id) {
        LaborCaseResponseDto response = laborCaseService.getCase(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaborCaseResponseDto> updateCase(
            @PathVariable Long id,
            @RequestBody LaborCaseRequestDto requestDto) {
        LaborCaseResponseDto response = laborCaseService.updateCase(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCase(@PathVariable Long id) {
        laborCaseService.deleteCase(id);
        return ResponseEntity.ok().build();
    }
}