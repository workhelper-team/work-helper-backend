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


// ============================================================
// 노동 사건 Service
// Controller에서 전달받은 요청을 실제 비즈니스 로직으로 처리
// ============================================================
@Service

// LaborCaseRepository를 생성자를 통해 자동 주입
@RequiredArgsConstructor

// 기본적으로 조회 작업은 읽기 전용 트랜잭션으로 처리
@Transactional(readOnly = true)
public class LaborCaseService {

    // 노동 사건의 DB 저장 및 조회를 담당하는 Repository
    private final LaborCaseRepository laborCaseRepository;


    // ============================================================
    // 노동 사건 생성
    // POST /api/cases
    // ============================================================
    @Transactional
    public LaborCaseResponseDto createCase(LaborCaseRequestDto requestDto) {

        // --------------------------------------------------------
        // 1. Request DTO의 데이터를 이용해 LaborCase Entity 생성
        // --------------------------------------------------------
        LaborCase laborCase = LaborCase.builder()
                .title(requestDto.getTitle())
                .category(requestDto.getCategory())
                .status(requestDto.getStatus())
                .summary(requestDto.getSummary())
                .build();


        // --------------------------------------------------------
        // 2. 생성한 사건을 DB에 저장
        // --------------------------------------------------------
        LaborCase savedCase =
                laborCaseRepository.save(laborCase);


        // --------------------------------------------------------
        // 3. 저장된 Entity를 Response DTO로 변환하여 반환
        // --------------------------------------------------------
        return new LaborCaseResponseDto(savedCase);
    }


    // ============================================================
    // 전체 노동 사건 조회
    // GET /api/cases
    // ============================================================
    public List<LaborCaseResponseDto> getAllCases() {

        // DB에서 모든 사건을 조회한 후
        // 각 Entity를 Response DTO로 변환
        return laborCaseRepository.findAll().stream()
                .map(LaborCaseResponseDto::new)
                .collect(Collectors.toList());
    }


    // ============================================================
    // 특정 노동 사건 조회
    // GET /api/cases/{id}
    // ============================================================
    public LaborCaseResponseDto getCase(Long id) {

        // 전달받은 ID에 해당하는 사건 조회
        LaborCase laborCase = laborCaseRepository.findById(id)

                // 사건이 존재하지 않으면 예외 발생
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 사건을 찾을 수 없습니다. ID: " + id
                        )
                );


        // 조회한 Entity를 Response DTO로 변환하여 반환
        return new LaborCaseResponseDto(laborCase);
    }


    // ============================================================
    // 노동 사건 수정
    // PUT /api/cases/{id}
    // ============================================================
    @Transactional
    public LaborCaseResponseDto updateCase(
            Long id,
            LaborCaseRequestDto requestDto
    ) {

        // --------------------------------------------------------
        // 1. 수정할 사건이 존재하는지 확인
        // --------------------------------------------------------
        LaborCase laborCase = laborCaseRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 사건을 찾을 수 없습니다. ID: " + id
                        )
                );


        // --------------------------------------------------------
        // 2. Entity의 updateCase()를 호출하여 기존 데이터 수정
        // --------------------------------------------------------
        laborCase.updateCase(
                requestDto.getTitle(),
                requestDto.getCategory(),
                requestDto.getStatus(),
                requestDto.getSummary()
        );


        // --------------------------------------------------------
        // 3. 수정된 Entity를 Response DTO로 변환
        // --------------------------------------------------------
        // JPA의 변경 감지를 통해 트랜잭션 종료 시 DB에 반영
        return new LaborCaseResponseDto(laborCase);
    }


    // ============================================================
    // 노동 사건 삭제
    // DELETE /api/cases/{id}
    // ============================================================
    @Transactional
    public void deleteCase(Long id) {

        // --------------------------------------------------------
        // 1. 삭제할 사건이 존재하는지 확인
        // --------------------------------------------------------
        LaborCase laborCase = laborCaseRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 사건을 찾을 수 없습니다. ID: " + id
                        )
                );


        // --------------------------------------------------------
        // 2. 해당 사건을 DB에서 삭제
        // --------------------------------------------------------
        laborCaseRepository.delete(laborCase);
    }
}