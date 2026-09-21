package com.workhelper.domain.expert.entity;

import com.workhelper.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "expert_profiles", schema = "app")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

/**
 * 노무사 테이블 연동
 */
public class ExpertProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expert_id")
    private Long expertId;


    // User 엔티티와 1:1 연관관계 설정
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(nullable = false, length = 100)
    private String organization;

    @Column(name = "license_file_path", nullable = false, length = 500)
    private String licenseFile;

    @Column(nullable = false, length = 20)
    private String status; // PENDING / APPROVED / REJECTED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ExpertProfile(User user, String licenseNumber, String organization, String licenseFile) {
    this.user = user;
    this.licenseNumber = licenseNumber;
    this.organization = organization;
    this.licenseFile = licenseFile;
    this.status = "PENDING";
    }   
    // 수동으로 setStatus 메서드 추가
    public void setStatus(String status) {
        this.status = status;
    }
}