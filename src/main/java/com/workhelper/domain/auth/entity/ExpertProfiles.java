package com.workhelper.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expert_profiles", schema = "app")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

/**
 * 노무사 테이블 연동
 */
public class ExpertProfiles {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expert_id")
    private Long expertId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "license_number")
    private String licenseNumber;

    private String organization;

    @Column(name = "license_file_path")
    private String licenseFile;

    private String status;

    // 값을 쉽게 넣기 위한 커스텀 생성자
    public ExpertProfiles(Long userId, String licenseNumber, String organization, String licenseFile) {
        this.userId = userId;
        this.licenseNumber = licenseNumber;
        this.organization = organization;
        this.licenseFile = licenseFile;
        this.status = "PENDING"; // 가입 초기 상태
    }
}