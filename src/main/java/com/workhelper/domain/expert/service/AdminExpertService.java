package com.workhelper.domain.expert.service;

import com.workhelper.domain.user.entity.User;
import com.workhelper.domain.expert.dto.AdminExpertDto;
import com.workhelper.domain.expert.entity.ExpertProfile;
import com.workhelper.domain.expert.repository.ExpertRepository;
import com.workhelper.infra.storage.EvidenceStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminExpertService {

    private final ExpertRepository expertRepository;
    private final EvidenceStorageService evidenceStorageService;

    // 노무사 가입 신청 목록 조회
    public List<AdminExpertDto.Response> getExpertApplications() {
        List<ExpertProfile> profiles = expertRepository.findAll();
        
        return profiles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 노무사 가입 신청 목록 조회
    private AdminExpertDto.Response toResponse(ExpertProfile profile) {
        AdminExpertDto.Response dto = new AdminExpertDto.Response();
        dto.setExpertId(profile.getExpertId());
        dto.setName(profile.getUser().getName());
        dto.setEmail(profile.getUser().getEmail());
        dto.setOrganization(profile.getOrganization());     
        dto.setLicenseNumber(profile.getLicenseNumber());
        dto.setStatus(profile.getStatus());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }
    
    // 노무사 가입 신청 상세 조회
    public AdminExpertDto.Response getExpertDetail(Long expertId) {
        ExpertProfile profile = expertRepository.findById(
                Objects.requireNonNull(expertId, "expertId는 필수입니다."))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 노무사 신청 정보입니다."));

        return toResponse(profile);
    }

    // 노무사 자격증 사본 조회
    public Resource loadLicenseFile(Long expertId) {
        ExpertProfile profile = expertRepository.findById(
                Objects.requireNonNull(expertId, "expertId는 필수입니다."))
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 노무사 신청 정보입니다."));

        return evidenceStorageService.load(profile.getLicenseFile());
    }


    // 노무사 권한 업데이트 
    @Transactional
    public List<AdminExpertDto.StatusUpdateResponse> updateExpertStatuses(List<Long> expertIds, String status) {
        List<ExpertProfile> profiles = expertRepository.findAllById(
            Objects.requireNonNull(expertIds, "expertIds는 필수입니다."));

        // 복수 ID가 선택 될 수 있으므로 for문 사용
        for (ExpertProfile profile : profiles) {
            profile.setStatus(status);

            // 승인(APPROVED)일 때만 User의 role도 EXPERT로 변경
            if ("APPROVED".equals(status)) {
                User user = profile.getUser();
                user.changeRole("EXPERT");
            } else if ("REJECTED".equals(status)) {
                User user = profile.getUser();
                user.changeRole("GENERAL");
            }
        }
        expertRepository.saveAllAndFlush(profiles);

        return profiles.stream()
                .map(profile -> {
                    AdminExpertDto.StatusUpdateResponse response = new AdminExpertDto.StatusUpdateResponse();
                    response.setExpertId(profile.getExpertId());
                    response.setStatus(profile.getStatus());
                    response.setUpdatedAt(profile.getUpdatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }
}