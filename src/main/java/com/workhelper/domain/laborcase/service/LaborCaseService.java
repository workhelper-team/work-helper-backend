package com.workhelper.domain.laborcase.service;

import com.workhelper.domain.consultation.entity.ConsultationMessage;
import com.workhelper.domain.consultation.entity.MessageRole;
import com.workhelper.domain.consultation.repository.ConsultationMessageRepository;
import com.workhelper.domain.laborcase.dto.LaborCaseRequestDto;
import com.workhelper.domain.laborcase.dto.LaborCaseResponseDto;
import com.workhelper.domain.laborcase.dto.LaborCaseUpdateRequestDto;
import com.workhelper.domain.laborcase.entity.CaseCategory;
import com.workhelper.domain.laborcase.entity.CaseStatus;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LaborCaseService {

    private final LaborCaseRepository laborCaseRepository;
    private final ConsultationMessageRepository consultationMessageRepository;

    // ============================================================
    // 사건 생성
    // POST /api/cases
    // ============================================================
    @Transactional
    public LaborCaseResponseDto createCase(
            Long userId,
            LaborCaseRequestDto requestDto
    ) {

        // 현재 MVP에서는 WAGE만 허용
        if (requestDto.getCategory() != CaseCategory.WAGE) {
            throw new IllegalArgumentException(
                    "현재 지원하는 사건 유형은 WAGE입니다."
            );
        }

        LaborCase laborCase = LaborCase.builder()
                .userId(userId)
                .title(requestDto.getTitle())
                .category(requestDto.getCategory())
                .status(CaseStatus.CREATED)
                .build();

        LaborCase savedCase = laborCaseRepository.save(laborCase);

        // initialDescription이 존재하면
        // 사건의 첫 번째 USER 상담 메시지로 저장
        if (requestDto.getInitialDescription() != null
                && !requestDto.getInitialDescription().isBlank()) {

            ConsultationMessage message =
                    ConsultationMessage.builder()
                            .laborCase(savedCase)
                            .role(MessageRole.USER)
                            .content(requestDto.getInitialDescription())
                            .structuredResult(null)
                            .build();

            consultationMessageRepository.save(message);
        }

        return new LaborCaseResponseDto(savedCase);
    }

    // ============================================================
    // 사건 목록 조회
    // GET /api/cases
    // ============================================================
    public Page<LaborCaseResponseDto> getCases(
            Long userId,
            String status,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<LaborCase> cases;

        if (status != null && !status.isBlank()) {

            CaseStatus caseStatus =
                    CaseStatus.valueOf(status.toUpperCase());

            cases = laborCaseRepository.findByUserIdAndStatus(
                    userId,
                    caseStatus,
                    pageable
            );

        } else {

            cases = laborCaseRepository.findByUserId(
                    userId,
                    pageable
            );
        }

        return cases.map(LaborCaseResponseDto::new);
    }

    // ============================================================
    // 사건 상세 조회
    // GET /api/cases/{caseId}
    // ============================================================
    public LaborCaseResponseDto getCase(
            Long caseId,
            Long userId
    ) {

        LaborCase laborCase =
                laborCaseRepository
                        .findByCaseIdAndUserId(caseId, userId)
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
    // PATCH /api/cases/{caseId}
    // ============================================================
    @Transactional
    public LaborCaseResponseDto updateCase(
            Long caseId,
            Long userId,
            LaborCaseUpdateRequestDto requestDto
    ) {

        LaborCase laborCase =
                laborCaseRepository
                        .findByCaseIdAndUserId(caseId, userId)
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