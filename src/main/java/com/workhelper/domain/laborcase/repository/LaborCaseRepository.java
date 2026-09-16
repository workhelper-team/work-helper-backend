package com.workhelper.domain.laborcase.repository;

import com.workhelper.domain.laborcase.entity.LaborCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaborCaseRepository extends JpaRepository<LaborCase, Long> {
}