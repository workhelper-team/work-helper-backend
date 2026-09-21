package com.workhelper.domain.expert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workhelper.domain.expert.entity.ExpertProfile;
import java.util.List;
import java.util.Optional;

public interface ExpertRepository extends JpaRepository<ExpertProfile, Long> {
    
    //유저 ID 찾기 위해 추가
    Optional<ExpertProfile> findByUser_UserId(Long userId);

    //관리자: 노무사 가입 신청 목록 조회를 위해 추가
    List<ExpertProfile> findByStatus(String status);
}