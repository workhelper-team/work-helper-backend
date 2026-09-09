package com.workhelper.domain.cases.service;

import com.workhelper.domain.cases.dto.CaseCreateRequest;
import com.workhelper.domain.cases.dto.CaseResponse;
import com.workhelper.domain.cases.entity.Case;
import com.workhelper.domain.cases.repository.CaseRepository;
import com.workhelper.domain.user.entity.User;
import com.workhelper.domain.user.repository.UserRepository;
import com.workhelper.global.error.BusinessException;
import com.workhelper.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    @Transactional
    public CaseResponse createCase(CaseCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Case caseEntity = Case.builder()
                .user(user)
                .title(request.title())
                .description(request.description())
                .caseType(request.caseType())
                .build();

        return CaseResponse.from(caseRepository.save(caseEntity));
    }

    public CaseResponse getCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));
        return CaseResponse.from(caseEntity);
    }

    public List<CaseResponse> getCasesByUser(Long userId) {
        return caseRepository.findByUserId(userId).stream()
                .map(CaseResponse::from)
                .toList();
    }

    @Transactional
    public CaseResponse updateStatus(Long caseId, Case.CaseStatus status) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CASE_NOT_FOUND));
        caseEntity.updateStatus(status);
        return CaseResponse.from(caseEntity);
    }
}
