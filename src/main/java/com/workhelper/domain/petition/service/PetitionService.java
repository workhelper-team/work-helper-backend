package com.workhelper.domain.petition.service;

import com.workhelper.domain.cases.entity.Case;
import com.workhelper.domain.cases.repository.CaseRepository;
import com.workhelper.domain.petition.dto.PetitionCreateRequest;
import com.workhelper.domain.petition.dto.PetitionResponse;
import com.workhelper.domain.petition.entity.Petition;
import com.workhelper.domain.petition.repository.PetitionRepository;
import com.workhelper.global.error.BusinessException;
import com.workhelper.global.error.ErrorCode;
import com.workhelper.infra.ai.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetitionService {

    private final PetitionRepository petitionRepository;
    private final CaseRepository caseRepository;
    private final AiClient aiClient;

    /**
     * FastAPI에 진정서 생성을 요청하고 결과를 저장
     */
    @Transactional
    public PetitionResponse generate(PetitionCreateRequest request) {
        Case caseEntity = caseRepository.findById(request.caseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));

        // TODO: 실제 FastAPI 연동 시 주석 해제
        // String generated = aiClient.generatePetition(request.facts());
        String generated = "AI 서버 연동 전 임시 진정서 내용입니다.\n[사실관계]\n" + request.facts();

        Petition petition = Petition.builder()
                .caseEntity(caseEntity)
                .title("진정서 - 사건 #" + caseEntity.getId())
                .content(generated)
                .build();

        return PetitionResponse.from(petitionRepository.save(petition));
    }

    public PetitionResponse getPetition(Long petitionId) {
        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PETITION_NOT_FOUND));
        return PetitionResponse.from(petition);
    }

    public List<PetitionResponse> getPetitionsByCase(Long caseId) {
        return petitionRepository.findByCaseEntityId(caseId).stream()
                .map(PetitionResponse::from)
                .toList();
    }

    @Transactional
    public PetitionResponse updateContent(Long petitionId, String content) {
        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PETITION_NOT_FOUND));
        petition.updateContent(content);
        return PetitionResponse.from(petition);
    }
}
