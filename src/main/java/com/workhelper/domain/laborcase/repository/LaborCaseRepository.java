package com.workhelper.domain.laborcase.repository;

import com.workhelper.domain.laborcase.entity.CaseStatus;
import com.workhelper.domain.laborcase.entity.LaborCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LaborCaseRepository
        extends org.springframework.data.jpa.repository.JpaRepository<LaborCase, Long> {

    Page<LaborCase> findByUserId(
            Long userId,
            Pageable pageable
    );

    Page<LaborCase> findByUserIdAndStatus(
            Long userId,
            CaseStatus status,
            Pageable pageable
    );

    Optional<LaborCase> findByCaseIdAndUserId(
            Long caseId,
            Long userId
    );
}