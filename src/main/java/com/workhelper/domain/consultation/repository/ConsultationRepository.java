package com.workhelper.domain.consultation.repository;

import com.workhelper.domain.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByLaborCaseIdOrderByIdAsc(Long laborCaseId);
}