package com.workhelper.domain.auth.repository;

import com.workhelper.domain.auth.entity.ExpertProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExpertRepository extends JpaRepository<ExpertProfiles, Long> {

    Optional<ExpertProfiles> findByUserId(Long userId);
}