package com.workhelper.domain.laborcase.service;

import com.workhelper.domain.laborcase.dto.LaborCaseRequestDto;
import com.workhelper.domain.laborcase.dto.LaborCaseResponseDto;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LaborCaseService {

    private final LaborCaseRepository laborCaseRepository;

    @Transactional
    public LaborCaseResponseDto createCase(LaborCaseRequestDto requestDto) {
        LaborCase laborCase = LaborCase.builder()
                .title(requestDto.getTitle())
                .category(requestDto.getCategory())
                .status(requestDto.getStatus())
                .summary(requestDto.getSummary())
                .build();

        LaborCase savedCase = laborCaseRepository.save(laborCase);
        return new LaborCaseResponseDto(savedCase);
    }

    public List<LaborCaseResponseDto> getAllCases() {
        return laborCaseRepository.findAll().stream()
                .map(LaborCaseResponseDto::new)
                .collect(Collectors.toList());
    }

    public LaborCaseResponseDto getCase(Long id) {
        LaborCase laborCase = laborCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사건을 찾을 수 없습니다. ID: " + id));
        return new LaborCaseResponseDto(laborCase);
    }

    @Transactional
    public LaborCaseResponseDto updateCase(Long id, LaborCaseRequestDto requestDto) {
        LaborCase laborCase = laborCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사건을 찾을 수 없습니다. ID: " + id));

        laborCase.updateCase(
                requestDto.getTitle(),
                requestDto.getCategory(),
                requestDto.getStatus(),
                requestDto.getSummary()
        );

        return new LaborCaseResponseDto(laborCase);
    }

    @Transactional
    public void deleteCase(Long id) {
        LaborCase laborCase = laborCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사건을 찾을 수 없습니다. ID: " + id));
        laborCaseRepository.delete(laborCase);
    }
}