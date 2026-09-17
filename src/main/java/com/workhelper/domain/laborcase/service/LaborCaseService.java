package com.workhelper.domain.laborcase.service;

import com.workhelper.domain.laborcase.dto.LaborCaseRequestDto;
import com.workhelper.domain.laborcase.dto.LaborCaseResponseDto;
import com.workhelper.domain.laborcase.dto.LaborCaseUpdateRequestDto;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// ============================================================
// 노동 사건 Service
//
// 노동 사건 생성, 조회, 수정 등의 비즈니스 로직을 담당
// ============================================================

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LaborCaseService {

    private final LaborCaseRepository laborCaseRepository;

    // ============================================================
    // 사건 생성
    //
    // POST /api/cases
    //
    // 생성 요청에서는
    // title, category, initialDescription을 전달받음
    //
    // 새로운 사건은 기본적으로 IN_PROGRESS 상태로 생성
    // ============================================================

    @Transactional
    public LaborCaseResponseDto createCase(
            LaborCaseRequestDto requestDto
    ) {

        LaborCase laborCase = LaborCase.builder()
                .title(requestDto.getTitle())
                .category(requestDto.getCategory())
                .status("IN_PROGRESS")
                .summary(requestDto.getInitialDescription())
                .build();

        LaborCase savedCase =
                laborCaseRepository.save(laborCase);

        return new LaborCaseResponseDto(savedCase);
    }

    // ============================================================
    // 사건 목록 조회
    //
    // GET /api/cases
    //
    // 현재는 전체 사건을 조회
    //
    // 사용자별 조회(user_id)는 User 연동 후 추가
    // ============================================================

    public List<LaborCaseResponseDto> getCases() {

        return laborCaseRepository.findAll()
                .stream()
                .map(LaborCaseResponseDto::new)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 사건 상세 조회
    //
    // GET /api/cases/{caseId}
    // ============================================================

    public LaborCaseResponseDto getCase(Long caseId) {

        LaborCase laborCase =
                laborCaseRepository.findById(caseId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "해당 사건을 찾을 수 없습니다. ID: "
                                                + caseId
                                )
                        );

        return new LaborCaseResponseDto(laborCase);
    }

    // ============================================================
    // 사건 정보 및 상태 수정
    //
    // PATCH /api/cases/{caseId}
    //
    // title, category, status, summary는 모두 선택값
    // null이 아닌 값만 수정
    //
    // 종료 및 보관 역시 status 변경으로 처리
    // ============================================================

    @Transactional
    public LaborCaseResponseDto updateCase(
            Long caseId,
            LaborCaseUpdateRequestDto requestDto
    ) {

        LaborCase laborCase =
                laborCaseRepository.findById(caseId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "해당 사건을 찾을 수 없습니다. ID: "
                                                + caseId
                                )
                        );

        laborCase.updateCase(
                requestDto.getTitle(),
                requestDto.getCategory(),
                requestDto.getStatus(),
                requestDto.getSummary()
        );

        return new LaborCaseResponseDto(laborCase);
    }
}
