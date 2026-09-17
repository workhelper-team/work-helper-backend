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
//
// Controller에서 전달받은 요청을 실제 비즈니스 로직으로 처리
// Repository를 통해 노동 사건 데이터를 저장, 조회, 수정, 삭제
// ============================================================
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LaborCaseService {

    // ============================================================
    // 노동 사건 DB 접근을 담당하는 Repository
    // ============================================================
    private final LaborCaseRepository laborCaseRepository;


    // ============================================================
    // 노동 사건 생성
    //
    // POST /api/cases
    //
    // Request DTO의 데이터를 Entity로 변환한 후
    // Repository를 통해 DB에 저장
    // ============================================================
    @Transactional
    public LaborCaseResponseDto createCase(
            LaborCaseRequestDto requestDto
    ) {

        // --------------------------------------------------------
        // 1. Request DTO → LaborCase Entity 변환
        // --------------------------------------------------------
        LaborCase laborCase = LaborCase.builder()
                .title(requestDto.getTitle())
                .category(requestDto.getCategory())
                .status(requestDto.getStatus())
                .summary(requestDto.getSummary())
                .build();


        // --------------------------------------------------------
        // 2. Entity를 Repository를 통해 DB에 저장
        // --------------------------------------------------------
        LaborCase savedCase =
                laborCaseRepository.save(laborCase);


        // --------------------------------------------------------
        // 3. 저장된 Entity → Response DTO 변환
        // --------------------------------------------------------
        return new LaborCaseResponseDto(savedCase);
    }


    // ============================================================
    // 전체 노동 사건 조회
    //
    // GET /api/cases
    //
    // Repository에서 전체 사건을 조회하고
    // 각각의 Entity를 Response DTO로 변환하여 반환
    // ============================================================
    public List<LaborCaseResponseDto> getAllCases() {

        return laborCaseRepository.findAll()
                .stream()
                .map(LaborCaseResponseDto::new)
                .collect(Collectors.toList());
    }


    // ============================================================
    // 특정 노동 사건 조회
    //
    // GET /api/cases/{id}
    //
    // 전달받은 사건 ID를 기준으로
    // 특정 사건을 조회
    // ============================================================
    public LaborCaseResponseDto getCase(Long id) {

        // --------------------------------------------------------
        // 1. 사건 ID를 기준으로 DB 조회
        // --------------------------------------------------------
        LaborCase laborCase = laborCaseRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 사건을 찾을 수 없습니다. ID: " + id
                        )
                );


        // --------------------------------------------------------
        // 2. 조회한 Entity → Response DTO 변환
        // --------------------------------------------------------
        return new LaborCaseResponseDto(laborCase);
    }


    // ============================================================
    // 노동 사건 수정
    //
    // PUT /api/cases/{id}
    //
    // 기존 사건을 조회한 후
    // Entity의 수정 메서드를 호출하여 데이터를 변경
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
        // 2. 기존 Entity의 데이터를 수정
        //
        // Entity 내부의 updateCase()를 호출하여
        // 전달받은 값으로 기존 사건 정보를 변경
        // --------------------------------------------------------
        laborCase.updateCase(
                requestDto.getTitle(),
                requestDto.getCategory(),
                requestDto.getStatus(),
                requestDto.getSummary()
        );


        // --------------------------------------------------------
        // 3. 수정된 Entity를 Response DTO로 변환
        //
        // 별도의 save() 호출 없이도
        // @Transactional 환경에서 JPA 변경 감지를 통해
        // 트랜잭션 종료 시 변경 내용이 DB에 반영됨
        // --------------------------------------------------------
        return new LaborCaseResponseDto(laborCase);
    }


    // ============================================================
    // 노동 사건 삭제
    //
    // DELETE /api/cases/{id}
    //
    // 사건 ID를 기준으로 기존 사건을 조회한 후
    // Repository를 통해 DB에서 삭제
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
        // 2. 조회한 사건을 Repository를 통해 삭제
        // --------------------------------------------------------
        laborCaseRepository.delete(laborCase);
    }
}